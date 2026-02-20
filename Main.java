package org.example;

import static org.example.MathZq.multiplyMatrices;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) { /**
     * Test de type MP* : Vérifier que notre fonction marche avant d'aller plus loin.
     */

        // On choisit un tout petit modulo pour vérifier les calculs de tête
        int q = 7;

        // Matrice A (2 lignes, 3 colonnes)
        int[][] A = {
                {1, 2, 3},
                {4, 5, 6}
        };

        // Matrice B (3 lignes, 2 colonnes)
        int[][] B = {
                {7, 8},
                {9, 1},
                {2, 3}
        };

        System.out.println("Lancement de la multiplication matricielle Modulo " + q + "...\n");
        int[][] C = multiplyMatrices(A, B, q);

        // Affichage du résultat
        System.out.println("Matrice Résultat C :");
        for (int i = 0; i < C.length; i++) {
            System.out.print("[ ");
            for (int j = 0; j < C[0].length; j++) {
                System.out.print(C[i][j] + " ");
            }
            System.out.println("]");
        }
    }
}