# Plan

## Le jeu du Sokoban

* Slide Hiroyuki : présentation très succinte du jeu (sans les règles), juste le créateur et l’année de création
  * Quoi -> Jeu vidéo
  * Qui -> Hiroyuki Imabayashi
  * Quand -> 1981
  * Difficultés -> a intéressé beaucoup de joueurs du fait de sa difficulté malgré ses règles simples + PSPACE !!!!
* Slide règles : règle de déplacement du joueur + but du jeu
  * but du jeu : pousser chaque caisse sur une cible
  * joueur se déplace case par case dans les 4 directions
  * peut se déplacer si
    * case suivante est libre (libre = sol OU cible)
    * case suivante est caisse ET caisse suivante suivante est libre
* Slide tuiles : présentation des différents types de tuiles
  * jeu en tile mapping : grille à 2 dimensions avec un nombre fini de tuiles différentes
  * mur, sol, caisse, cible, caisse sur cible
  * représentation en mémoire : tableau 2D
* Slide problématique + apport personnel
* Slide lien avec le thème de l’année
  * Slide entrepôt: dans les villes actuelles, il y a de plus en plus de colis à livrer. sokoban permet de simuler la gestion d’un entrepôt (optimisation des déplacements etc.)
  * Slide ville :
    * changement d’échelle, ici la ville entière est vue comme la carte du sokoban. on simule alors un problème de livraison de colis : caisse = colis, cible = destination du colis (maison de qqn par exemple) et joueur = livreur
    * particulièrement justifié pour villes à plan hippodamien (rues perpendiculaires), ex New York


## Principe de résolution

* Concept d’état : tout ce qui caractérise une configuration du jeu
  * position du joueur
  * position des caisses
* Arbre des états
  * Principe :
    * partir de l’état initial du niveau
    * pour chaque coup possible
      * si configuration gagnante (chaque caisse sur une cible)
        on remonte alors les états pour avoir la solution
      * sinon on l’ajoute à la liste des configurations à considérer
* 2 problématiques majeures :
  * dans quel ordre traiter les configurations (BFS, DFS, etc)
  * présence de configurations qui rendent le jeu insoluble (deadlocks, cf après) -> à éviter !
  * Première difficulté : présence de cycles (résolu facilement)
* En réalité, graphe car on peut retomber sur le même état de différentes façons (cf exemple)
  (pas de slide) Utilisation d’un HashSet pour stocker les états déjà vus (table de transposition) :
  Pour chaque nouvel état considéré, calculer son hash
  Si déjà dans le set, on l’ignore
* Calcul du hash : valeurs de Zobrist. On associe pour chaque case deux valeurs: caisse (0) et joueur (1). Pour hacher une configuration, on XOR la valeur caisse de chaque case contenant une caisse et la valeur joueur de la case contenant le joueur. Repose sur l’associativité, commutativité hash le fait que a XOR a = 0. hash XOR T[i][0], ‘ajoute’ la caisse au i au plateau dans le hash ou la ‘supprime’.

## Réduction de l'espace de recherche
### Statique

* Statique:
  tout de qui ne dépend pas des états
  intérêt : peut être précalculé une bonne fois pour toutes avant d’explorer l’arbre des états
* dead tiles:
  enlever toutes les caisses
  pour chaque cible :
  placer une caisse dessus
  la tirer tant que c'est possible, et marquer chaque case visitée comme non morte
  Les cases non atteintes sont les dead tiles.
* détection de tunnels
  * Slide 1: introduction aux tunnels, difficile à définir, intuitivement carré en jaune sur diapo.
  * Slide 2-5: avantages des tunnels.
    * Slide 2 : tunnel ne peut contenir plus de deux caisses
    * Slide 3 : au plus deux états fils, inutile de déplacer au milieu.
    * Slide 4 : 1 seul état quand il y a un coin car on veut potentiellement la stocker dans le tunnel puis la sortir (si on la pousse plus -> deadlock).
    * Slide 5: tunnel avec une seule direction: inutile de laisser la caisse au milieu du tunnel. Pour les détecter: vider le plateau, placer un mur dans le tunnel et voir si plateau divisé en deux.
    * Slide 29 : Comment détecter les tunnels: recherche de motif. On trouve un motif puis on étend des deux côtés le tunnel tant qu'on reconnaît un motif de tunnel. (on tourne les motifs)
* FRAME: Salles et packing order
  * Slide 1 : Salle = tout ce qui n’est pas tunnel. Idée: trouver un ordre de rangement: lorsqu’on pousse une caisse dans une salle, directement la déplacer au bon endroit si possible.
  * Slide 2 : Partir de la salle remplie. À l’aide d’un BFS, trouver les caisses atteignables depuis l’entrée de la salle. Puis tirer une des caisses atteignables jusqu’à l’entrée. Si possible, la supprimer et recommencer. Si impossible pour toute alors il n’y a pas de packing order
  * Slide 3 : Packing order obtenu
  * Slide 4 : Un exemple où ça ne fonctionne pas. (Il faut déplacer une caisse à l’endroit du joueur puis ranger l’autre)

### Dynamique

* FRAME: détection d’impasse
  * 3 exemples de deadlocks. Deadlocks = configuration des caisses et du joueur tel qu’il est impossible de parvenir à l’état final depuis cette configuration.
* FRAME: détection de freeze deadlocks
  * Slide 1: Algorithme récursif, caisse gelée si gelée sur les 2 axes X/Y. Gelée sur un axe si une des trois conditions est remplie. cf schéma. règle 2: les deux sols adjacents dans l’axe sont des dead tiles. Si caisse, on fait un appel récursif en remplaçant la caisse par un mur.
  * Slide 2-5: exemple.
* FRAME: détection de PI Corral deadlock
  * Slide 1: corral = zone entouré par des caisses. I corral: corral mais toutes les caisses sur la barrière peuvent être uniquement pousser à l'intérieur du corral. PI corral: I corral mais toutes les caisses sur la barrière sont accessibles par le joueur et une caisse n’empêche pas une poussée. Intérêt des PI corrals: on peut enlever toutes les caisses qui ne sont pas dedans/sur la barrière/bloqué par la barrière. C’est un deadlock si plus aucun mvt possible. Pas un deadlock si une caisse sort du corral / toutes les caisses sont sur une cible. Sous niveau => limite de temps
  * Slide 2: Multi PI corral: on fusionne les corrals tant que l’on peut et que ça ne forme pas un PI corral.
  * Ce sont des deadlocks très fréquent
* FRAME: tables de deadlock
  * Slide 1: Tables de deadlock: motif de taille 4x4, utilisé après une poussée (donc caisse + joueur) + nouveau deadlock. On compare en partant de 1 jusqu’à 14.
  * Slide 2: On a un deadlock si on ne peut bouger toutes les caisses sur la bordure. On construit alors un arbre: on part de 1, on essaye les 3 possibilités, regarde si c’est un deadlock, puis on répète. Quand c’est un deadlock, inutile de regarder les enfants.

## Recherche dirigée par une heuristique

(slide de titre)
Jusqu’ici, aucune priorité donné l’ordre dans lequel on traite les états
Or, certains états sont plus prometteurs que d’autres et méritent d’être considérés avant les autres
Utilisation d’une heuristique pour déterminer la proximité entre un état donné et la solution
Différentes heuristique possibles (compromis précision / rapidité de calcul)
(fait) Simple lower bound
somme des distances entre les caisses et leur cible la plus proche
O(n) -> rapide, peu précis (parfois plusieurs caisses sur même cible, pas réaliste)
(fait) Greedy lower bound
on trie les distances caisses → cibles
on prend le minimum, on supprime les caisses/cibles déjà utilisées
O(n²) avec optimisation (cf après), assez rapide, moyennement précis
Algorithme hongrois (optionnel) :
non implémenté, O(n³), lent mais précis
permet de détecter un type de deadlock → bipartite deadlock
Algorithme FESS
introduire rapidement l’idée de l’algo



## Optimisations

Heuristique “Greedy lower bound”
Comment marquer des nœuds / cibles comme vues et tous les démarquer en O(1) ?
solution: Soit m un entier. chaque nœud n_i se voit attribuer un entier m_i.
n_i est marqué ssi m_i == m.
pour marquer un nœud, il suffit d'affecter m à m_i.
pour tous les démarquer, il suffit d'incrémenter m.
Limite : si on ne marque pas un nœud pendant ≈ 2^32 itérations, il va se retrouver marqué alors qu'il ne l'est pas

Algo greedy:
liste chaînée des caisses qui ne sont pas sur des cibles. Pointe vers un tableau qui à un cible (haut) associe la distance de la caisse à la cible (bas). La flèche indique quelle case du tableau l’algo va lire en premier. S' il y a une croix, il saute la case.
FRAME: Calcul des corrals
Slide 1: soit parcours en profondeur sur chaque case O(W * H * (W * H + 4 * W * H)), coûteux. On va utiliser Union-Find pour avoir du O(wh). Remarque: le corral d’une case vide est le même que celle du corral au dessus (si au dessus vide) et que celui du corral à gauche (si à gauche vide)
Slide 2: relation de récurrence. ATTENTION: la structure Union-Find est potentiellement violée pour les indices >= y * w + x. Le but de la relation de récurrence est justement de la rendre valide pour y * w + x.

## Résultats, conclusion

Limites des techniques utilisées:
Pb tunnels
Pb détection dead positions
Résultats cf plus tard

* Beaucoup de choses non implémentées :
    * diagonal deadlock
    * algorithme hongrois
    * packing order est limité
