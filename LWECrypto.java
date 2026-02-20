package org.example;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LWECrypto {
    private final LWEParams params;
    private final Random rand;

    public LWECrypto() {
        this.params = new LWEParams();
        this.rand = new Random();
    }

    /**
     * L'algorithme de génération de clés de Regev (LWE)
     */
    public LWEKeyPair generateKeyPair() {
        int n = params.n;
        int m = params.m;
        int q = params.q;

        // ÉTAPE 1 : Générer la clé secrète 's' (Vecteur colonne n x 1)
        int[][] s = new int[n][1];
        for (int i = 0; i < n; i++) {
            s[i][0] = rand.nextInt(q); // Nombre aléatoire entre 0 et q-1
        }

        // ÉTAPE 2 : Générer la matrice publique 'A' (m x n)
        int[][] A = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = rand.nextInt(q);
            }
        }

        // ÉTAPE 3 : Générer le vecteur d'erreur gaussienne 'e' (m x 1)
        // C'est grâce à cette erreur que l'ordinateur quantique ne peut pas casser le code !
        int[][] e = MathZq.generateGaussianError(m, q, params.stdDev);

        // ÉTAPE 4 : Calculer B = (A * s) + e (modulo q)
        // C'est ici qu'on utilise notre superbe classe MathZq !
        int[][] As = MathZq.multiplyMatrices(A, s, q);
        int[][] B = MathZq.addMatrices(As, e, q);

        // ÉTAPE 5 : Emballer les clés dans nos objets
        LWEKeyPair.PublicKey pk = new LWEKeyPair.PublicKey(A, B);
        LWEKeyPair.PrivateKey sk = new LWEKeyPair.PrivateKey(s);

        return new LWEKeyPair(pk, sk);
    }
    /**
     * CHIFFREMENT : Cache un seul bit (0 ou 1) à l'intérieur d'un système matriciel.
     * @param messageBit Le bit à cacher (0 ou 1)
     * @param pk La clé publique du destinataire
     */
    public LWECipherText encryptBit(int messageBit, LWEKeyPair.PublicKey pk) {
        if (messageBit != 0 && messageBit != 1) {
            throw new IllegalArgumentException("Erreur : Le message doit être un bit binaire (0 ou 1).");
        }

        int m = params.m;
        int q = params.q;

        // 1. Générer le vecteur aléatoire r (composé uniquement de 0 et de 1)
        int[][] r = new int[m][1];
        for (int i = 0; i < m; i++) {
            r[i][0] = rand.nextInt(2); // Donne aléatoirement 0 ou 1
        }

        // 2. Calculer u = (A^T * r) mod q
        int[][] AT = MathZq.transpose(pk.A);
        int[][] u = MathZq.multiplyMatrices(AT, r, q);

        // 3. Calculer v = (B^T * r) + Message * (q/2) mod q
        int[][] BT = MathZq.transpose(pk.B);
        int[][] BTr = MathZq.multiplyMatrices(BT, r, q); // Renvoie une matrice 1x1

        int v_scalar = BTr[0][0];

        // C'est ici qu'on "encode" le message. Si c'est 1, on l'envoie au milieu du modulo (q/2).
        int shift = messageBit * (q / 2);
        int v = Math.floorMod(v_scalar + shift, q);

        return new LWECipherText(u, v);
    }

    /**
     * DÉCHIFFREMENT : Retire le bruit pour retrouver le bit d'origine.
     * @param cipherText Le message chiffré (u, v)
     * @param sk La clé privée du destinataire (s)
     */
    public int decryptBit(LWECipherText cipherText, LWEKeyPair.PrivateKey sk) {
        int q = params.q;

        // 1. Calculer s^T * u
        int[][] sT = MathZq.transpose(sk.s);
        int[][] sTu = MathZq.multiplyMatrices(sT, cipherText.u, q); // Renvoie une matrice 1x1

        // 2. Équation de déchiffrement brut : Résultat = v - (s^T * u) mod q
        int rawResult = Math.floorMod(cipherText.v - sTu[0][0], q);

        // 3. Suppression du bruit (Prise de décision mathématique)
        // Le bruit a modifié le résultat. On regarde s'il est plus proche de 0 ou de q/2.
        int distanceToZero = Math.min(rawResult, q - rawResult); // Distance par rapport à 0 ou q
        int distanceToHalfQ = Math.abs(rawResult - (q / 2));     // Distance par rapport à q/2

        // Si c'est plus proche de zéro, le bit caché était 0, sinon c'était 1.
        if (distanceToZero < distanceToHalfQ) {
            return 0;
        } else {
            return 1;
        }
    }
    /**
     * CHIFFREMENT D'UNE PHRASE : Convertit le texte en bits et chiffre chaque bit.
     */
    public List<LWECipherText> encryptString(String text, LWEKeyPair.PublicKey pk) {
        List<LWECipherText> encryptedMessage = new ArrayList<>();

        // Convertit le texte en tableau d'octets (bytes)
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            // Pour chaque octet, on extrait ses 8 bits (de gauche à droite)
            for (int i = 7; i >= 0; i--) {
                int bit = (b >> i) & 1; // Opération binaire pour lire le bit
                encryptedMessage.add(encryptBit(bit, pk));
            }
        }
        return encryptedMessage;
    }

    /**
     * DÉCHIFFREMENT D'UNE PHRASE : Déchiffre les bits et les réassemble en texte.
     */
    public String decryptString(List<LWECipherText> cipherTexts, LWEKeyPair.PrivateKey sk) {
        int numOfBytes = cipherTexts.size() / 8;
        byte[] decryptedBytes = new byte[numOfBytes];

        int bitIndex = 0;
        for (int i = 0; i < numOfBytes; i++) {
            byte b = 0;
            // On reconstruit l'octet bit par bit
            for (int j = 7; j >= 0; j--) {
                int bit = decryptBit(cipherTexts.get(bitIndex), sk);
                b = (byte) (b | (bit << j)); // Opération binaire pour écrire le bit
                bitIndex++;
            }
            decryptedBytes[i] = b;
        }
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    public static void main(String[] args) {
        LWECrypto crypto = new LWECrypto();

        System.out.println("--- 1. GÉNÉRATION DES CLÉS ---");
        LWEKeyPair keys = crypto.generateKeyPair();
        System.out.println("Clés générées.");

        System.out.println("\n--- 2. TEST DE CHIFFREMENT DE TEXTE ---");
        String phraseOriginale = "Bonjour Mesdames et Messieurs, je suis autodidacte en mathématiques approfondies.";
        System.out.println("Message clair : \"" + phraseOriginale + "\"");

        long startTime = System.currentTimeMillis();
        List<LWECipherText> texteChiffre = crypto.encryptString(phraseOriginale, keys.publicKey);
        long endTime = System.currentTimeMillis();

        System.out.println("Message chiffré ! Le texte a été transformé en " + texteChiffre.size() + " équations matricielles.");
        System.out.println("Temps de chiffrement : " + (endTime - startTime) + " ms");

        // Affichons juste à quoi ressemble la première lettre chiffrée pour prouver que c'est illisible
        System.out.println("Aperçu du premier bit chiffré : v=" + texteChiffre.get(0).v);

        System.out.println("\n--- 3. TEST DE DÉCHIFFREMENT ---");
        String phraseDechiffree = crypto.decryptString(texteChiffre, keys.privateKey);
        System.out.println("Message déchiffré : \"" + phraseDechiffree + "\"");

        if (phraseOriginale.equals(phraseDechiffree)) {
            System.out.println("\n[SUCCÈS TOTAL] pour l'algorithme LWE ");
        } else {
            System.out.println("\n[ERREUR] Le message a été altéré.");
        }
    }
    /* public static void main(String[] args) {
        LWECrypto crypto = new LWECrypto();

        System.out.println("--- 1. GÉNÉRATION DES CLÉS ---");
        LWEKeyPair keys = crypto.generateKeyPair();
        System.out.println("Clé publique et clé secrète générées avec succès.");

        System.out.println("\n--- 2. TEST DE CHIFFREMENT (Le bit 1) ---");
        int bitOriginal = 1;
        System.out.println("Message original : " + bitOriginal);

        LWECipherText chiffré = crypto.encryptBit(bitOriginal, keys.publicKey);
        System.out.println("Message chiffré intercepté par un hacker :");
        System.out.println("v = " + chiffré.v);
        System.out.println("u[0][0] = " + chiffré.u[0][0] + " ... (et " + (chiffré.u.length - 1) + " autres valeurs illisibles)");

        System.out.println("\n--- 3. TEST DE DÉCHIFFREMENT ---");
        int bitRetrouve = crypto.decryptBit(chiffré, keys.privateKey);
        System.out.println("Message déchiffré avec la clé secrète : " + bitRetrouve);

        if (bitOriginal == bitRetrouve) {
            System.out.println("\n[SUCCÈS] Les mathématiques fonctionnent. Le système est inviolable !");
        } else {
            System.out.println("\n[ÉCHEC] Le bruit était trop fort, la donnée a été corrompue.");
        }
    }*/
    // Test rapide pour voir si ça ne plante pas
   /* public static void main(String[] args) {
        System.out.println("Initialisation du système Post-Quantique LWE...");
        LWECrypto crypto = new LWECrypto();

        System.out.println("Génération des clés en cours...");
        LWEKeyPair keys = crypto.generateKeyPair();

        System.out.println("Génération réussie !");
        System.out.println("Dimension de la matrice publique A : " + keys.publicKey.A.length + "x" + keys.publicKey.A[0].length);
        System.out.println("Dimension du secret s : " + keys.privateKey.s.length + "x" + keys.privateKey.s[0].length);
    }*/
}