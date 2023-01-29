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

Le Sokoban est un jeu vidéo inventé en 1981 par Hiroyuki Imabayashi. Il consiste à déplacer des caisses vers des cibles dans un labyrinthe en deux dimensions. Le joueur est astreint à se déplacer selon les quatre directions (nord, sud, est, ouest). Il ne peut bouger qu'une seule caisse à la fois et ne peut pas les tirer. Ces règles simples en font un jeu populaire, bien que difficile [2].

En effet, il a été démontré que la résolution d'un Sokoban est un problème NP-difficile et PSPACE-complet [4]. Timo Virkkala [2] explique que la difficulté du Sokoban provient à la fois de la très grande profondeur et du facteur de branchement (qui peut aller jusqu'à 100) de l'arbre des possibilités. Ainsi, on se concentre plutôt sur la recherche d'une solution et non sur son optimalité, ce qui en fait un problème de recherche. La notion de solution optimale est d'ailleurs ambiguë pour le Sokoban, selon qu'on parle du nombre de déplacements du joueur ou du nombre de poussées de caisses.

En 1999, Andreas Junghanns présente <i>Rolling Stone</i>, le premier solveur de Sokoban, dans un article qui introduit les bases des concepts de résolution. On y trouve, entre autres : la détection de configurations qui rendent le jeu insoluble (<i>deadlocks</i>), l'utilisation d'une table de transposition pour ne pas traiter plusieurs fois la même configuration, la mise en place de mouvements à grande échelle (<i>macro moves</i>) pour éviter de générer des états intermédiaires et le calcul d’une heuristique - qui se base sur la distance entre les caisses et les objectifs - pour guider sa recherche. C'est aussi dans cet article qu'est introduit l'ensemble de 90 niveaux qui sert depuis de référence pour tester les performances des solveurs [1].

Les solveurs postérieurs à <i>Rolling Stone</i> ont introduit différentes approches.
Par exemple, <i>Powerplan</i> voit le Sokoban comme un graphe abstrait de tunnels et de salles. Cette approche n’est cependant pas vraiment meilleure que celle de <i>Rolling Stone</i>, puisque <i>Powerplan</i> ne résout que 10 niveaux là où <i>Rolling Stone</i> en résolvait 54 [6].
Timo Virkkala formalise la notion d'enclos (<i>corral</i>), qui désigne une zone du niveau inaccessible au joueur à cause de caisses qui en obstruent les accès. Il examine aussi plusieurs moyens de détecter les <i>deadlocks</i>, comme par exemple le calcul de positions mortes (<i>dead positions</i>), des cases dont l'emplacement fait que le niveau devient insoluble si une caisse est poussée dessus [2].
Un autre solveur, <i>GroupEffort</i>, utilise simultanément différents algorithmes de recherche qui travaillent de manière indépendante (pas de synchronisation), les seules informations partagées étant les configurations insolubles [4].
Le premier solveur capable de résoudre l'ensemble des 90 niveaux, appelé <i>FESS</i> (pour <i>FEeature Search Space</i>), est présenté en 2020 par Yaron Shoham et Jonathan Schaeffer. Là où la plupart des autres solveurs n'utilisent qu'une seule heuristique dans leur recherche, <i>FESS</i> se distingue par son utilisation combinée de plusieurs heuristiques différentes. Il crée ainsi un espace abstrait, nommé <i>Feature Space</i>, qu'il explore conjointement avec l'arbre des possibilités du Sokoban pour trouver une solution [7].

En plus des articles présentant des solveurs, on peut trouver différentes stratégies aidant à la résolution dans la littérature.
Albert. L. Zobrist propose dans une fonction de hachage efficace pour pouvoir stocker les configurations de jeu déjà visitées lors de la recherche [5].
Le site Web <i>sokobano.de</i> introduit d’autres types de deadlock (<i>freeze deadlock</i>, <i>bipartite deadlock</i> etc.) et propose des méthodes de détection. Il contient aussi de nombreuses statistiques sur les différents solveurs (issus de la littérature ou non).

Bien que de nombreuses idées soient présentées, peu de ressources indiquent comment les mettre en œuvre de façon concrète. Implémenter ces algorithmes de manière efficace sera donc un des enjeux de ce TIPE.

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

* Jeu vidéo de réflexion (Puzzle video game)
* Problème de recherche (Search problem)
* PSPACE-complet (PSPACE-complete)
* Émondage de graphe (Graph pruning)
* Heuristique (Heuristic)

Liste de références bibliographiques (5 à 10 références)
========================================================

Numéro, Auteur, Titre, Référence et/ou URL

1
Andreas Junghanns
Pushing the Limits: New Developments in Single-Agent Search
Thesis (1999), University of Alberta, https://www.researchgate.net/publication/2305703_Pushing_the_Limits_New_Developments_in_Single-Agent_Search

2
Timo Virkkala
Solving Sokoban
Master's Thesis (2011), University of Helsinki, http://sokoban.dk/wp-content/uploads/2016/02/Timo-Virkkala-Solving-Sokoban-Masters-Thesis.pdf

3
sokobano.de
http://sokobano.de/wiki/index.php?title=Main_Page, consulté régulièrement entre 2022 et 2023

4
Nils Froleyks
Using an Algorithm Portfolio to Solve Sokoban
Bachelor Thesis (2016), Karlsruhe Institute of Technology, https://baldur.iti.kit.edu/theses/SokobanPortfolio.pdf

5
Albert L. Zobrist
A New Hashing Method with Application for Game Playing
Technical Report (1970), University of Wisconsin, https://minds.wisconsin.edu/bitstream/handle/1793/57624/TR88.pdf

6
A. Botea, M. Müller and J. Schaeffer
Using abstraction for planning in Sokoban
Proceedings of the 3rd International Conference on Computers and Games (2002), Springer, https://webdocs.cs.ualberta.ca/~mmueller/ps/botea-sokoban.pdf

7
Yaron Shoham and Jonathan Schaeffer
The FESS Algorithm: A Feature Based Approach to Single-Agent Search
IEEE Conference on Games (2020), https://ieee-cog.org/2020/papers/paper_44.pdf
