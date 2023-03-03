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

| Solver | Suite / Levels        | Time limit | Solved | Error / Other                           | Total | Start date       | End date         | Total run time    |
|--------|-----------------------|------------|--------|-----------------------------------------|-------|------------------|------------------|-------------------|
| DFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution               | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min         |
| DFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution                           | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min          |
| A*     | GroupEffort small set | 3 min      | 87     | 24 timeout - 89 errors                  | 200   | 15/02/2023 7:18  | 15/02/2023 8:56  | 1h 38min          |
| A*     | Large test suite      | 3min       | 2095   | 1171 timeout - 4 errors - 2 no solution | 3272  | 24/02/2023 19:21 | 27/02/2023 09:40 | 2 days 14h 19 min |

### Detailed results for 4-th line

#### State statistics
Total number of state explored: 963611941
Average state explored per report: 459957
Level with the least explored state: 0 - GrigrSpecial_40 #40 (all crates are on target...)
Level with the most explored state: 13555057 - Sven_1623 #143 (approx 7Gi RAM)

#### Time statistics
Total run time: 3 h 42 min 52.73 s (+ 3min * 1171 = 2.43 days)
Average run time per report: 6.38 s
Fastest solved level: in 0 ms - Microban II_135 #2
Slowest solved level: in 2 min 52.57 s - Sven_1623 #997

#### Solution length (moves)
Average solution length per report: 296
Level with shortest solution: 0 moves - GrigrSpecial_40 #40 (151 levels are solved in 0ms)
Level with longest solution: 5037 moves - Microban II_135 #134 (22 levels are solved in more than 2 min, 52 between 1 and 2 min)

#### Solution length (pushes)
Average solution length per report: 59
Level with shortest solution: 0 pushes - GrigrSpecial_40 #40
Level with longest solution: 616 pushes - Sven_1623 #45

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

Detect 0 length tunnel:
```
$$$$$$$$$$
$  $     $
$        $
$   $    $
$$$$$$$$$$
```

### PI corral

Multi pi corral
corral

## Resources

<a href="http://sokobano.de/wiki/" target="_blank"><img border="0" title="Go to SokobanWiki!" src="http://sokobano.de/wiki/images/Sokowiki-01.gif"></a>