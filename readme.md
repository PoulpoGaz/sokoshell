# sokoshell 1.0

sokoshell is a [sokoban](https://en.wikipedia.org/wiki/Sokoban) solver and game !
You can load (see [formats](#supported-formats)), play, solve sokoban in your favorite terminal.

## Usage

**Warning**: all version of Windows are not supported, even the most recent one which seems to have an appropriate terminal.

Current version doesn't offer a simple 'jar'. Therefore, to run it you will have to install the JDK 17 and maven.
Then run in your favorite terminal `bash sokoshell-in-terminal.bash`. If everything works, you will see:

```
Welcome to sokoshell - Version 1.0
Type 'help' to show help. More help for a command with 'help command'
sokoshell> 
```

Here, you can type commands as in a normal terminal. The 'help' command will guide you. (err..., it is not very explanatory currently)
Auto-completion is supported (use TAB), history (up, down arrows), auto-suggestions (left arrow) are supported.

You can load levels with 'load PATH' (levels are stored under [levels](levels)).
Solving can be done with 'solve -p PACK -l LEVEL_INDEX'
You will be notified when the solver finished to run. And you can watch the solver running
with the command 'monitor'. Results can be seen with 'list reports' and the solution with
'print solution -p PACK -l LEVEL_INDEX'.

### TODO

1. Help is not very helpful.
2. Redo pi-corral.
3. Help is really not very helpful
4. Add the new super auto-completer

## The solver

Four solvers currently exists:
* DFS (queue based)
* BFS (stack based)
* A* (two heuristics available, priority queue based)
* fess0 (two heuristics available, priority queue based)

All solvers detect dead tiles, freeze deadlocks, pi-corral, tunnels, rooms. A simple packing order is computed
for rooms with only one entrance. They also use goal cuts and deadlock table (4 x 4).

For A*, two heuristics are available to compute the distance to the final state:
* simple: juste sum the distance between all crates and their respective nearest crates. Run in $O(n)$
* greedy: The problem of the simple heuristic is that a target can be associated with multiples crates but a target
  can only contain one crate. A simple solution is to store in a huge array all minimum distances between crates and targets,
  sort the array, and associate a crate with a target and trying to minimize the sum. 
  This algorithm runs in $O(n^2 log n)$ but we created one in $O(n^2)$.

Finally, fess0 is a very simple version of Festival (as of 2023, the best solver). We just prioritize configurations
with the most crates on target, then with the lowest connectivity and finally we one of the two heuristics previously
defined.

For more information, see [resources](#resources)

### Results

Our solvers can, in the best configuration, solve 15/90 levels of XSokoban and 2273/3272 of large test suite.
You can compare with other existing solvers at [sokobano.de](http://sokobano.de/wiki/index.php?title=Solver_Statistics).

Ram limit = 32 GiB

| Solver | Suite / Levels        | Time limit | Solved | Error / Other                               | Total | Start date       | End date         | Total run time    | Detailed results                                                       |
|--------|-----------------------|------------|--------|---------------------------------------------|-------|------------------|------------------|-------------------|------------------------------------------------------------------------|
| DFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution                   | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min         | Not available                                                          |
| DFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution                               | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min          | Not available                                                          |
| A*     | GroupEffort small set | 3 min      | 87     | 24 timeout - 89 errors                      | 200   | 15/02/2023 7:18  | 15/02/2023 8:56  | 1h 38min          | Not available                                                          |
| A*     | Large test suite      | 3 min      | 2095   | 1171 timeout - 4 errors - 2 no solution     | 3272  | 24/02/2023 19:21 | 27/02/2023 09:40 | 2 days 14h 19min  | Not available                                                          |
| fess0  | XSokoban_90           | 10 min     | 14     | 76 timeout                                  | 90    | 03/03/2023 20:35 | 04/03/2023 09:20 | 12h 45min         | Not available                                                          |
| fess0  | large_text_suite      | 10 min     | 2273   | 946 timeout - 51 ram exceed - 2 no solution | 3272  | 16/04/2023 15:54 | 24/04/2023 02:21 | 7 days 10h 25min  | [sokoshell_1.0_astar](levels%2Flarge_test_suite%2Fsokoshell_1.0_astar) |
| A*     | large_test_suite      | 10 min     | 2204   | 1066 timeout - 2 no solution                | 3272  | 07/05/2023 15:38 | 15/05/2023 13:25 | 7 days 21h 53 min | [sokoshell_1.0_fess0](levels%2Flarge_test_suite%2Fsokoshell_1.0_fess0) |

### Ideas

#### Goal Packing Order

Problems:
* Only for rooms with one entrance.
* Can't creat packing order if intermediate packing is needed

Solutions:
* Room may have multiple entrance but only one that accept crate (not isPlayerOnlyTunnel)
* Generate all possibilities and store in a aPackage

#### Reachable tiles

possible optimization: https://en.wikipedia.org/wiki/Flood_fill
Probably efficient algorithm:
The algorithm will compute all zones of the map.
It will use a union-find structure. Two tiles in the same partition are in the same zone.
Recursive function;
```
computeZone(x, y) {
    if solid(x, y) then do nothing.
    else if solid(x - 1, y) && solid(x, y - 1) then do nothing
    else if solid(x - 1, y) then (x, y - 1) and (x, y) are in the same zone (UNION)
    else if solid(x, y - 1) then (x - 1, y) and (x, y) are in the same zone (UNION)
    else then (x, y - 1) and (x - 1, y) and (x, y) are in the same zone (UNION)
}
```
Using dynamic programming, the complexity of the above function is O(width x height).
I think it is possible to calculate topX, topY and create a list (or aPackage) containing all tile
in the same zone while keeping the same complexity.

New way of computing topX, topY after **one** push: if the crate is a frontier (ie delimits two zone),
topX, topY can be calculated as follows: if the crate moves inside the other zone, then 
topX, topY = max((topX, topY), (newCrateX, newCrateY)), if the crate moves inside the current zone then
topX, topY = if ((newCrateX, newCrateY) = (topX, topY)) then complicated (new to find a new topX, topY, maybe storing the second topX, topY)
             else max((topX, topY), otherZoneTopXY)

Thinking about this, it should also work for multiple push of the same crate (for the 2nd case only...)


#### Tunnels

Detect 0 length tunnel:
```
$$$$$$$$$$
$  $     $
$        $
$   $    $
$$$$$$$$$$
```

#### Deadlocks

Detect closed diagonal deadlocks


## Supported formats

* .xsb (http://sokobano.de/wiki/index.php?title=Level_format)
* .slc (format used by https://www.sourcecode.se/sokoban/levels.php)
* .8xv (format used by https://github.com/PoulpoGaz/Isekai)

Various levels by various authors are stored in the repository. In the case of non-compliance with a license, please create an issue.


## Developers

### Debugging in a an external terminal

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `bash sokoshell-in-terminal.bash -d` (d for debug)
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.

### Modules

* args: Code parsing user input, deserializing strings, injecting variables values with black magic (reflective API), executing appropriate commands
  and offer auto-completion and default commands to the main module (sokoshell).
  It is very similar to [picocli](https://picocli.info/), but it supports pipe.
* interval: A simple library to parse intervals (i.e. things of the form '5-10, 13, 15-')
* sokoshell: The main module. Contains the solver, file reading, all the commands and a basic GUI toolkit for terminal (inspired by Swing)
* tools: various tools used to create the presentation, removing duplicate levels and converting java to latex.


## History

The project was created in an academic context. In France, students in 'CPGE' have to
create a project and present it at the end of the CPGE. This is called a 'TIPE'. Therefore,
files under documents are folders related to the 'TIPE' (except documents/thesis).

However, for me (PoulpoGaz), sokoban solving started before this project, and you can see a first version
[here](https://github.com/PoulpoGaz/Isekai).

## Resources

A goldmine: <a href="http://sokobano.de/wiki/" target="_blank"><img border="0" title="Go to SokobanWiki!" src="http://sokobano.de/wiki/images/Sokowiki-01.gif"></a>

Multiple thesis are stored under documents/thesis. In the case of non-compliance with a license, please create an issue.