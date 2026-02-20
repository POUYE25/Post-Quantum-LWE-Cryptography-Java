package org.example;

public class LWEParams {
    // Les paramètres standard pour un niveau de sécurité basique
    public final int n = 128;         // Dimension de la clé secrète (le vecteur s)
    public final int m = 256;         // Nombre d'équations (doit être plus grand que n)
    public final int q = 3329;        // Le modulo premier (utilisé dans l'algorithme Kyber)
    public final double stdDev = 2.0; // L'écart-type pour la distribution de notre erreur gaussienne
}