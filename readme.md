# SokoShell

Version 1.0-SNAPSHOT

## Results

| Solver | Suite / Levels        | Time limit | Solved | Error / Other                               | Total | Start date       | End date         | Total run time    |
|--------|-----------------------|------------|--------|---------------------------------------------|-------|------------------|------------------|-------------------|
| DFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution                   | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min         |
| DFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution                               | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min          |
| A*     | GroupEffort small set | 3 min      | 87     | 24 timeout - 89 errors                      | 200   | 15/02/2023 7:18  | 15/02/2023 8:56  | 1h 38min          |
| A*     | Large test suite      | 3min       | 2095   | 1171 timeout - 4 errors - 2 no solution     | 3272  | 24/02/2023 19:21 | 27/02/2023 09:40 | 2 days 14h 19min  |
| fess0  | XSokoban_90           | 10min      | 14     | 76 timeout                                  | 90    | 03/03/2023 20:35 | 04/03/2023 09:20 | 12h 45min         |
| fess0¹ | large_text_suite      | 10min      | 2273   | 946 timeout - 51 ram exceed - 2 no solution | 3272  | 16/04/2023 15:54 | 24/04/2023 02:21 | 7 days 10h 25min  |
| A*²    | large_test_suite      | 10 min     | 2204   | 1066 timeout - 2 no solution                | 3272  | 07/05/2023 15:38 | 15/05/2023 13:25 | 7 days 21h 53 min |


¹:
²: For Sasquatch_VII_50 #48, ram limit was increased to 30 Gb and ran separately.
## Ideas

### Goal Packing Order

Problems:
* Only for rooms with one entrance.
* Can't creat packing order if intermediate packing is needed

Solutions:
* Room may have multiple entrance but only one that accept crate (not isPlayerOnlyTunnel)
* Generate all possibilities and store in a aPackage

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
I think it is possible to calculate topX, topY and create a list (or aPackage) containing all tile
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

### Deadlocks

Detect closed diagonal deadlocks

## Developers
### Debugging in a an external terminal

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `bash sokoshell-in-terminal.bash -d` (d for debug)
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.

## Resources

<a href="http://sokobano.de/wiki/" target="_blank"><img border="0" title="Go to SokobanWiki!" src="http://sokobano.de/wiki/images/Sokowiki-01.gif"></a>