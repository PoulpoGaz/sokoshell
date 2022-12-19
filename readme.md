# SokoShell

Version 0.1

### How to debug and keep autocompletion?

For IntelliJ IDEA:
* Create a `Remote JVM Debug` configuration. Values by default are great.
* Start sokoshell with `bash sokoshell-in-terminal.bash -d`
* Wait until the compilation is finished
* Start the previously created `Remote JVM Debug` configuration.


### TODO

| TODO                                                                                                                                      | DONE?          |
|-------------------------------------------------------------------------------------------------------------------------------------------|----------------|
| A*: pushes lower bound calculation                                                                                                        | in progress... |
| deadlocks:<ul><li>corral</li><li>closed diagonals</li><li>bipartite deadlocks</li></ul>                                                   | no             |
| goal cuts -> packing order                                                                                                                | no             |
| table comparing feature                                                                                                                   | no             |

## Results

| Solver | Suite / Levels        | Time limit | Solved | Error / Other             | Total | Start date       | End date         | Total run time |
|--------|-----------------------|------------|--------|---------------------------|-------|------------------|------------------|----------------|
| BFS    | Large test suite      | 30 sec     | 1732   | 8 errors - 46 no solution | 3272  | 10/12/2022 15:28 | 11/12/2022 04:43 | 13h 45min      |
| BFS    | GroupEffort small set | 30 sec     | 47     | 1 no solution             | 200   | 11/12/2022 16:19 | 11/12/2022 17:43 | 1h 24min       |
