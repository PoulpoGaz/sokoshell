# SokoShell

Version 0.1

### How to debug and keep autocompletion?

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `bash sokoshell-in-terminal.bash -d`
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.


### TODO

| TODO                                                                                                             | DONE?       |
|:-----------------------------------------------------------------------------------------------------------------|-------------|
| deadlocks:<ul><li>corral</li><li>closed diagonals</li><li>bipartite deadlocks</li></ul>                          | no          |
| A* pushes lower bound calculation :<ul><li>greedy</li><li>optimal</li><li>according to player position</li></ul> | in progress |
| goal cuts -> packing order                                                                                       | no          |
| charts comparing optimization                                                                                    | no          |

## Results

| Solver | Suite / Levels        | Time limit | Solved | Error / Other             | Total | Start date       | End date         | Total run time |
|--------|-----------------------|------------|--------|---------------------------|-------|------------------|------------------|----------------|
| DFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min      |
| DFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution             | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min       |

## Ideas

### Greedy heuristic

Slow: ~4000 nodes/sec
thesis: at least 20 000 nodes/sec
JSoko: ~500 nodes/sec but they use the non-greedy.

The major consequence is it is the worst results. Example:
Original & Extra: level 1 to 45
max-ram: 5GB
timeout: 1 min

A* simple solves 6 levels (1, 2, 3, 6, 17, 38).
DFS solves 5 levels (1, 2, 3, 6, 17).
A* greedy solves 3 levels (1, 6, 17).

Results are available under levels/levels8xv/Original.8xv.solutions.json.gz. They are automatically loaded by 
sokoshell. In sokoshell, use `list reports --pack "Original & Extra" --column-index 7` to show result and sort
by date. The first results are A* greedy, then A* simple and finally DFS.

Possible solutions:
* leave it like that and add pi corral pruning, one way tunnels, etc. to reduce the size of the tree
* IntelliJ Profiler shows that the major problems is the pop operation.
  But I think we can optimize considering that already matched CrateToTarget (crate/target used by another)
  must be discarded. The new pop operation will also remove matched CrateToTarget when moving a node down.
  With this implementation, pop will run in O(n). But pop will be called only n times because already matched
  are removed. Therefore, the complexity is now O(n²).
* MinHeap uses an ArrayList while it can use an array: using it make only sense when the content is fixed sized,
  need to be cached (like CrateToTarget in GreedyHeuristic) because the class already exists as PriorityQueue.
  Replacing the list by an array shouldn't increase performance a lot.
* When all crate/target are matched the while loop should stop. For this, adding a counter counting the number
  of crate/target matched should do the trick.

But everything isn't dark. A* greedy can almost solve Original & Extra level 4. For this level the problem
is the lack of deadlock detection.

### Map

Problems: 
* Level always returns a full copy, even if the caller doesn't need to modify the map.
* There is no distinction between a solver map and a map. The solver map usually holds more information than the map.
  This information aren't useful for PlayCommand and SolutionCommand
* The new version of style will be able to draw dead tiles, rooms, etc. But, trackers don't return a Map.
  Returning the map can be solution, but it will allow MonitorCommand to modify the map.
* Map clashes will java.util.Map

Conclusion:
* Map should be renamed (eg: Board).
* Level should be able to remove an immutable view of a board, as well as a full copy.
* Tracker should be able to return an immutable view of a solver board

Possible architecture:
IBoard: Interface; defines all read-only operation
Board: Implementation of IBoard; also have modification operation
SolverBoard: Implementation of IBoard; with modification operation and additional information used by solvers
SolverBoardView: Implementation of IBoard; with more read-only operation

### Styles

Styles are currently in development in branch poulpogaz. It will solve the following problems:
* impossibility of programmatically creating style.
* style from file cannot define how to draw dead tiles, rooms, etc.
* finding the cell at (x; y) is difficult. It will be possible to show x and y position on the top and the left
  of the map.
* Exporting map to png only works for style that only uses image and define style of size 16.

TODO:
* add documentation