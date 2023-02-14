This readme defines colors used by the presentation
Colors are in the following format: `red; green; blue; alpha` with red, green, blue
numbers between 0 and 255 inclusive and alpha a percentage

# Diagram

When updating these values, please also update styles.
* Tunnel mask: 200; 192; 100; 25%
* Room mask: 89; 133; 198; 25%
* Dead tile mask: 255; 0; 0; 25%

# Plan

## Le jeu du Sokoban

Quoi ? Qui ? Quand ? Difficulté ?

## Principe de résolution

* État
* BFS / DFS
* Recherche exhaustive
* Avec cycle → table de transposition, zobrist values

## Réduction de l'espace de recherche

### Statique

* dead tiles:
  * enlever les caisses
  * placer une caisse sur une cible
  * la tirer tant que c'est possible
  * répéter pour toutes les cibles
  * Les cases non atteintes = dead tiles
* tunnels, rooms, packing order
  * Objectifs => Macro moves
  * Tunnel = zone où la manœuvrabilité du joueur est limitée à 1.
    * Lorsqu'une caisse rentre dans un tunnel : au plus 2 états enfants.
    * Donne un algo, nécessité schéma. 
    * oneway → au plus un état enfant.
    * Un tunnel ne peut contenir 2 caisses.
    * Tunnel avec des coins → une caisse ne peut aller au bout, mais à l'entrée oui !
  * Salle = tout ce qui n'est pas tunnel
    * packing order
    * Calcul simple : on place toutes les caisses sur les cibles d'une salle puis on les tire toutes.
    * seulement pour salle avec une seule entrée

### Dynamique

* Deadlocks : généralisation dead tiles
* freeze deadlocks : bloc de caisse qui ne peut plus du tout bouger
  * caisse geler sur axe horizontal si :
    * mur à droite / gauche
    * dead tile à droite / gauche (vérifier si vraiment important)
    * caisse à droite / gaucher qui est gelée (nécessite appel récursif)
  * on vérifie pour chaque caisse sauf celle sur une cible.
* PI Corral deadlocks
  * Corral = enclos, zone inaccessible par le joueur
  * Délimité par caisse = barrière
  * I = inwards : toutes les caisses sur la barrière sont atteignables par le joueur, ne peuvent pas forcément être poussées
  * P = player : toutes les caisses sur la barrière peuvent être poussées vers l'intérieur du corral par le joueur
  * Intérêt des PI Corrals : on peut enlever toutes les caisses et se concentrer sur celles faisant partis du corral.
  * si une caisse sort du corral → pas un deadlock
  * toutes les caisses du corral sont sur une cible et aucune caisse n'est sans cible → pas un deadlock
  * plus aucun mouvement possible → deadlock
  * vu comme un sous niveau → nécessite limite de temps

## Recherche dirigée par une heuristique

A*. Différentes heuristiques

* Simple lower bound :
  * somme des distances entre les caisses et leur cible la plus proche
  * O(n) rapide, peux précis
* Greedy lower bound :
  * on trie les distances caisses → cibles
  * on prend le minimum, on supprime les caisses/cibles déjà utilisées
  * O(n²) avec optimization, assez rapide, moyennement précis
* Algorithme hongrois:
  * non implémenté, O(n³), lent mais précis
  * Permet de détecter un type de deadlock → bipartite deadlock 

## Optimizations

* Pour A*, greedy lower bound, comment marquer des nœuds / cibles comme vues et tous les démarquer en O(1) ? 
  * solution: Soit m un entier. chaque nœud n_i se voit attribuer un entier m_i. 
    n_i est marqué ssi m_i == m.
    Pour marquer un nœud, il suffit d'affecter m à m_i.
    Pour tous les démarquer, il suffit d'incrémenter m.
    Risque : si on ne marque pas un nœud pendant 2³² itérations. Il va se retrouver marquer alors qu'il ne l'est pas
* Algo greedy:
  Faire un beau schéma
* Calcul des corrals : soit parcours en profondeur sur chaque case => O(W * H * (W * H + 4 * W * H))
  * couteux.
  * usage structure union find
  * relation de récurrence :
    ```
    corral(x, y) {
      if solide alors singleton
      else {
        if !solide(x-1, y) alors union
        if !solide(x, y-1) alors union
      }
    }
    ```
    O(W * H) !

## Résultats, conclusion

* Beaucoup de choses non implémentées : 
  * diagonal deadlock
  * algorithme hongrois
  * packing order est limitée
