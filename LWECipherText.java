package org.example;

public class LWECipherText {
    public final int[][] u; // Le vecteur u
    public final int v;     // Le scalaire v (le bout de message caché dans le bruit)

    public LWECipherText(int[][] u, int v) {
        this.u = u;
        this.v = v;
    }
}