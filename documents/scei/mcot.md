Titre de votre sujet TIPE (20 mots maximum)
==========================================
Résolution de niveaux du Sokoban

Quelle est votre motivation pour le choix du sujet? (50 mots maximum)
=====================================================================

Depuis que j'ai découvert le Sokoban, un jeu de réflexion japonais, dans un tutoriel de programmation, il me fascine par sa difficulté malgré ses règles simples. Mon binôme connaissant aussi ce jeu, nous avons pensé que ce TIPE serait une bonne occasion de travailler sur ce sujet d'intérêt commun.

J'ai découvert le Sokoban, un jeu de réflexion japonais, dans le même tutoriel de programmation que mon binôme. Ce jeu nous fascine tous les deux par sa difficulté malgré ses règles simples. Nous avons donc pensé que ce TIPE serait une bonne occasion de travailler sur ce sujet d'intérêt commun.

En quoi votre étude s'inscrit-elle dans le thème de l'année ? (50 mots maximum)
===============================================================================

Le Sokoban, littéralement « gardien d'entrepôt », consiste à pousser des caisses sur des objectifs dans un labyrinthe. On simule ainsi la gestion d'un entrepôt ou un problème de livraison de colis (en assimilant le labyrinthe à une ville), deux enjeux importants dans les villes où les livraisons sont nombreuses.

Bibliographie commentée (au maximum 650 mots)
=============================================

Le Sokoban est un jeu vidéo inventé en 1981 par Hiroyuki Imabayashi consistant à déplacer des caisses vers des cibles dans un labyrinthe en deux dimensions. Le joueur est astreint à se déplacer selon les quatre directions (nord, sud, est, ouest). Il ne peut bouger qu'une seule caisse à la fois et ne peut pas les tirer. Ces règles simples en font un jeu populaire, bien que difficile. [timo]

En effet, il a été démontré que la résolution d'un Sokoban est NP-difficile et PSPACE-complet [portfolio]. Timo Virkkala [_] explique que la difficulté du Sokoban provient de la très grande profondeur de l'arbre des possibilités ainsi que du facteur de branchement qui peut aller jusqu'à 100. Ainsi, on se concentrera sur la recherche d'une solution et non sur son optimalité (pb de recherche). La notion de solution optimale est d'ailleurs ambigüe dans le Sokoban, selon qu'on parle du nombre de déplacements du joueur ou du nombre de poussées de caisses.

En 1999, (auteurs) présentent Rolling Stone, le premier solveur de Sokoban, dans un article qui introduit les bases des concepts de résolution : détection de configurations qui rendent le jeu insolvable (deadlocks), calcul d'une heuristique pour guider la recherche, [1 ..., ajouter 1 ?] C'est aussi dans cet article qu'est introduit l'ensemble de 90 niveaux qui sert depuis de référence pour tester les performances des solveurs [_].

Les solveurs postérieurs à Rolling Stone ont essayé différentes approches, tel Powerplan qui voit le Sokoban comme un graphe abstrait de tunnels et de salles [_]. Le premier solveur capable de résoudre l'ensemble des 90 niveaux, appelé FESS, est présenté en 2020 par (auteurs) [_]. Il introduit notamment [2 ...].

On trouve différentes stratégies aidant à la résolution dans la littérature. (time master thesis) donne plusieurs moyens de détecter les deadlocks, en [3 ...]. Différentes heuristiques sont présentées sur sokobano.de, ainsi que leur méthode de calcul, qui se base sur la distance entre les caisses et les objectifs. Enfin, un article de A. L. Zobrist [_] propose une fonction de hachage efficace pour pouvoir stocker les configurations de jeu déjà visitées lors de la
recherche.

Cependant, peu d'articles indiquent comment mettre ces idées en œuvre. Implémenter ces algorithmes de manière efficace sera donc un des enjeux de ce TIPE.

* [1 ...]
* [2 ...]
* [3 ...]

<small>
* Sokoban = casse tête inventé en ~1980 par ~Hiroyuki Imabayashi
* Règles simples, jeu populaire (timo virkkala)
* Pourtant difficile à résoudre : NP-difficile et PSPACE-complet (portfolio)
* Plusieurs définitions d'une solution optimale : déplacements du joueur, poussées de caisses
* ==> Problème de recherche et non d'optimisation
* Aucun solveur actuel ne résout tout ce qui existe : cependant, meilleur solveur actuel (FESS) résout les 90 niveaux de
l'ensemble des niveaux qui sert de référence pour évaluer les performances des solveurs (cog)
* 
* Calcul du hash d'un état (Zobrist)
* Détection des deadlocks (Time master thesis)
* Calcul d'heuristique (sokobano.de)
* Tunnels, salles, packing order (Using abstraction for planning)
</small>



Problématique retenue (au maximum 50 mots)
==========================================

Quelles stratégies adopter pour trouver une solution le plus rapidement possible à un niveau de Sokoban ?

Objectifs du TIPE (au maximum 100 mots)
=======================================

1. Concevoir un algorithme généraliste pouvant résoudre, en théorie, n'importe quel niveau de Sokoban, sans chercher une solution optimale. 
2. Analyser les niveaux afin d'identifier des motifs récurrents (deadlocks, tunnels, etc.) pour élaborer des stratégies visant à accélérer la recherche.
3. Implémenter cet algorithme et ces stratégies en Java.
4. Effectuer des tests de performances sur des ensembles de niveaux pour comparer notre solveur à ceux qui existent déjà.

Positionnements thématiques* et mots-clés* (français et anglais)
================================================================

INFORMATIQUE (Informatique pratique)
INFORMATIQUE (Informatique Théorique)

* Jeu vidéo de réflexion (puzzle video game)
* Problème de recherche (Search problem)
* PSPACE-complet (PSPACE-complete)
* Émondage de graphe (Graph pruning)
* Heuristique (Heuristic)


Liste de références bibliographiques (5 à 10 références)
========================================================

Auteur 1, Auteur 2… Nom du périodique, Titre de l'article Volume (Année), Pages…

[2] Timo Virkkala, Solving Sokoban, Master's Thesis, University of Helsinki (2011)
[7] http://sokobano.de/wiki/index.php?title=Main_Page, consulté entre 2022 et 2023
[3] Nils Froleyks, Using an Algorithm Portfolio to Solve Sokoban, Bachelor Thesis, Karlsruhe Institute of Technology (2016)
[5] Andreas Junghanns, Pushing the Limits: New Developments in Single-Agent Search, Thesis, University of Alberta (1999)
[4] A. Botea, M. Müller and J. Schaeffer, Using abstraction for planning in sokoban, Proceedings of the 3rd International Conference on Computers and Games, Springer, 2002
[4] A. Botea, M. Müller and J. Schaeffer, Using abstraction for planning in sokoban, Conference Paper, Lecture Notes in Computer Science (July 2002)
[1] Albert L. Zobrist, A New Hashing Method with Application for Game Playing, University of Wisconsin Technical Report (1970)
[8] Yaron Shoham and Jonathan Schaeffer, The FESS Algorithm: A Feature Based Approach to Single-Agent Search, IEEE Conference on Games (2020)
