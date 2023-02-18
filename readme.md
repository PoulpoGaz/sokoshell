# SokoShell

Version 0.1

### How to debug and keep autocompletion?

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `bash sokoshell-in-terminal.bash -d`
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.


### TODO

| TODO                                                                                                                                                   | DONE?       |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------|-------------|
| deadlocks:<ul><li>corral</li><li>closed diagonals</li><li>bipartite deadlocks</li></ul>                                                                | no          |
| A* pushes lower bound calculation :<ul><li>greedy</li><li>real distance (a* pathfinder)</li><li>optimal</li><li>according to player position</li></ul> | in progress |
| goal cuts -> packing order                                                                                                                             | no          |
| charts comparing optimization                                                                                                                          | no          |

## Results

| Solver | Suite / Levels        | Time limit | Solved | Error / Other             | Total | Start date       | End date         | Total run time |
|--------|-----------------------|------------|--------|---------------------------|-------|------------------|------------------|----------------|
| DFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min      |
| DFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution             | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min       |
| A*     | GroupEffort small set | 3 min      | 87     | 24 timeout - 89 errors    | 200   | 15/02/2023 7:18  | 15/02/2023 8:56  | 1h 38min       |

## Ideas

### Goal Packing Order

Problems:
* Only for rooms with one entrance.
* Can't creat packing order if intermediate packing is needed

Solutions:
* Room may have multiple entrance but only one that accept crate (not isPlayerOnlyTunnel)
* Generate all possibilities and store in a tree

### Reachable tiles

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
I think it is possible to calculate topX, topY and create a list (or tree) containing all tile
in the same zone while keeping the same complexity.

New way of computing topX, topY after **one** push: if the crate is a frontier (ie delimits two zone),
topX, topY can be calculated as follows: if the crate moves inside the other zone, then 
topX, topY = max((topX, topY), (newCrateX, newCrateY)), if the crate moves inside the current zone then
topX, topY = if ((newCrateX, newCrateY) = (topX, topY)) then complicated (new to find a new topX, topY, maybe storing the second topX, topY)
             else max((topX, topY), otherZoneTopXY)

Thinking about this, it should also work for multiple push of the same crate (for the 2nd case only...)


### Tunnels

Remove playerOnlyTunnel with one exit.