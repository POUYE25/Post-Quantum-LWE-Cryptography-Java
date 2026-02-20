package org.example;

public class LWEKeyPair {
    public final PublicKey publicKey;
    public final PrivateKey privateKey;

    public LWEKeyPair(PublicKey pk, PrivateKey sk) {
        this.publicKey = pk;
        this.privateKey = sk;
    }

    // --- Sous-classe : La Clé Publique (A, B) ---
    public static class PublicKey {
        public final int[][] A; // Matrice aléatoire (m x n)
        public final int[][] B; // Vecteur résultat B = As + e (m x 1)

        public PublicKey(int[][] A, int[][] B) {
            this.A = A;
            this.B = B;
        }
    }

    // --- Sous-classe : La Clé Privée (s) ---
    public static class PrivateKey {
        public final int[][] s; // Le secret (n x 1)

        public PrivateKey(int[][] s) {
            this.s = s;
        }
    }
}