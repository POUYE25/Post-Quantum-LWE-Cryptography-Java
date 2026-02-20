#  Post-Quantum Cryptography : Implémentation LWE (Java From Scratch)

**Auteur :** Alassane POUYE | Étudiant en L3 Mathématiques-Informatique  
**Statut :** Projet personnel de recherche et développement  

## Contexte du Projet

Avec l'avènement imminent des ordinateurs quantiques, l'algorithme de Shor menace de briser les systèmes cryptographiques asymétriques actuels (RSA, Courbes Elliptiques - ECC). Pour anticiper cette menace, la cryptographie post-quantique repose sur de nouveaux paradigmes mathématiques, notamment la géométrie des réseaux euclidiens.

Ce projet est une implémentation **"From Scratch" (sans aucune bibliothèque mathématique externe)** du cryptosystème **LWE (Learning With Errors)**, introduit par Oded Regev.

L'objectif de ce projet est de démontrer ma capacité à traduire des concepts d'algèbre linéaire avancée et d'arithmétique modulaire en une architecture logicielle robuste (Java Orienté Objet).

##  L'Architecture Mathématique

L'implémentation repose strictement sur des opérations matricielles au sein d'un corps fini $\mathbb{Z}_q$.
Le cœur de la sécurité de LWE repose sur l'ajout d'une erreur (bruit gaussien), rendant le système d'équations impossible à résoudre même pour un ordinateur quantique.

* **Clé secrète ($s$) :** Un vecteur aléatoire $\in \mathbb{Z}_q^n$.
* **Clé publique ($A, B$) :** Où $A$ est une matrice aléatoire $\in \mathbb{Z}_q^{m \times n}$ et $B$ est calculé selon l'équation :
    $$B = (A \times s) + e \pmod q$$
    *(Où e est un vecteur d'erreur tiré selon une distribution gaussienne discrète).*



## Spécificités Techniques (Java)

Pour garantir la pureté de l'implémentation, je n'ai utilisé aucune bibliothèque externe type *Apache Commons Math*. J'ai développé mon propre moteur d'algèbre linéaire (`MathZq.java`).

1.  **Arithmétique Modulaire Stricte :** Remplacement systématique de l'opérateur modulo classique de Java (`%`) par `Math.floorMod()` pour garantir des valeurs strictement positives dans $\mathbb{Z}_q$.
2.  **Prévention des Overflows :** Utilisation du type `long` lors des produits matriciels intermédiaires avant l'application du modulo $q$, garantissant l'intégrité de la mémoire.
3.  **Génération d'Erreur Gaussienne :** Implémentation d'une distribution normale centrée sur 0, arrondie discrètement pour le corps fini.
4.  **Chiffrement de texte :** Conversion de chaînes de caractères (UTF-8) en flux binaires, où chaque bit est chiffré individuellement dans l'espace matriciel.

## Utilisation

Le point d'entrée du programme se trouve dans la classe `LWECrypto.java`. L'exécution du `main` réalise les actions suivantes :
1. Génération des clés publiques et privées.
2. Chiffrement d'une chaîne de caractères complète en de multiples équations matricielles.
3. Déchiffrement et élimination du bruit gaussien pour restituer le message clair.
