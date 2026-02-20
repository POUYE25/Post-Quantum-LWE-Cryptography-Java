package org.example;

import java.util.Random;

public class MathZq {


    /**
     * Multiplie deux matrices A et B dans le corps fini Z_q (Modulo q)
     */
    public static int[][] multiplyMatrices(int[][] A, int[][] B, int q) {
        int rowsA = A.length;
        int colsA = A[0].length;
        int rowsB = B.length;
        int colsB = B[0].length;

        // 1. Règle stricte d'algèbre linéaire : les colonnes de A doivent égaler les lignes de B
        if (colsA != rowsB) {
            throw new IllegalArgumentException("Erreur : Dimensions incompatibles pour la multiplication matricielle.");
        }

        // 2. Création de la matrice résultat C
        int[][] C = new int[rowsA][colsB];

        // 3. Le triple calcul (Lignes x Colonnes)
        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {

                //  On utilise un 'long' (64 bits) pour la somme.
                long sum = 0;

                for (int k = 0; k < colsA; k++) {
                    sum += (long) A[i][k] * B[k][j];
                }

                // garantit que le nombre est tout petit (inférieur à q).
                C[i][j] = (int) Math.floorMod(sum, q);
            }
        }
        return C;
    }
    /**
     * 2. ADDITION MATRICIELLE dans Z_q
     * Utile pour calculer B = (A * s) + e
     */
    public static int[][] addMatrices(int[][] A, int[][] B, int q) {
        int rows = A.length;
        int cols = A[0].length;

        if (rows != B.length || cols != B[0].length) {
            throw new IllegalArgumentException("Les matrices doivent avoir la même taille pour l'addition.");
        }

        int[][] C = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // On additionne et on applique le modulo q pour rester dans le corps fini
                C[i][j] = Math.floorMod((long)A[i][j] + (long)B[i][j], q);

            }
        }
        return C;
    }
    /**
     * 3. TRANSPOSITION D'UNE MATRICE (A^T)
     * Renverse les lignes et les colonnes, indispensable pour le chiffrement.
     */
    public static int[][] transpose(int[][] A) {
        int rows = A.length;
        int cols = A[0].length;

        int[][] T = new int[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                T[j][i] = A[i][j];
            }
        }
        return T;
    }
    /**
     * 4. LA GÉNÉRATION DE L'ERREUR GAUSSIENNE DISCRÈTE (Le cœur de LWE)
     * @param size La taille du vecteur d'erreur (généralement m)
     * @param q Le modulo
     * @param stdDev L'écart-type de la cloche (généralement 2.0)
     */
    public static int[][] generateGaussianError(int size, int q, double stdDev) {
        int[][] e = new int[size][1]; // On crée un vecteur colonne (matrice size x 1)
        Random rand = new Random();

        for (int i = 0; i < size; i++) {
            // nextGaussian() génère un nombre à virgule centré sur 0 avec un écart-type de 1
            // On le multiplie par notre stdDev pour élargir un peu la cloche
            double val = rand.nextGaussian() * stdDev;

            // On arrondit à l'entier le plus proche (car on travaille dans un corps discret)
            int intVal = (int) Math.round(val);


            // pour qu'il devienne un entier positif valide dans Z_q
            e[i][0] = Math.floorMod(intVal, q);
        }
        return e;
    }
}