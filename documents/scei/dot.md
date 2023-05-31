1. Mai 2022 : Recherche du sujet et formulation de la problématique : décision de se restreindre à l’existence d’une solution 
   (et non à la recherche d’une solution optimale). Familiarisation avec les recherches déjà effectuées dans ce domaine.
2. Juin 2022 : Première version du solveur : recherche par force brute sans optimisation via un parcours en profondeur de 
   l’arbre des états. Utilisation du hash de Zobrist [5] pour éviter les cycles. Cette approche permet de résoudre les niveaux simples, 
   et suffit étonnamment pour certains niveaux de difficulté moyenne.
3. Juillet - août 2022 : Début de recherche de deadlock : ajout de la détection des dead tiles et des freeze deadlocks. Cependant, 
   ces optimisations n’augmentent pas beaucoup le nombre de niveaux résolus, mais contribuent tout de même à la réduction de l’espace de recherche.
4. Septembre - octobre 2022 : Détection de tunnels et de salles. Calcul d’un ordre de rangement d’une salle dans un cas simple : 
   pour celles comportant seulement une entrée et possédant au moins une cible.
5. Décembre à janvier 2023 : Mise en place d'une recherche guidée à l'aide d'une heuristique, à la manière de A*. 
   Implémentation de deux heuristiques, simple et gourmande. Optimisation de la deuxième pour réduire sa complexité.
6. Février à mars 2023 : Ajout de la détection de PI-Corral deadlocks [3]. Cependant, les résultats ne sont pas satisfaisants 
   d’où la décision de considérer les I-Corrals plutôt que les PI-Corrals.
7. Mars 2023 : Implémentation de FESS [7], très partielle, mais suffisante pour résoudre des niveaux qui paraissaient jusqu’alors hors de portée.
8. Avril - début juin 2023 : Lancement du solveur sur les deux ensembles de niveaux principaux. Comparaison des résultats avec 
   les autres solveurs existants et réalisation de la présentation.

