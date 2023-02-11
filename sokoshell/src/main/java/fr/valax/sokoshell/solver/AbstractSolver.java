package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * May be removed in the future
 * @author darth-mole
 * @author PoulpoGaz
 */
public abstract class AbstractSolver implements Solver {

    protected final String name;

    AbstractSolver(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    // http://www.sokobano.de/wiki/index.php?title=How_to_detect_deadlocks
    protected boolean checkFreezeDeadlock(Board board, State state) {
        int[] crates = state.cratesIndices();

        for (int crate : crates) {
            TileInfo info = board.getAt(crate);

            if (info.isCrate() && checkFreezeDeadlock(info)) {
                return true;
            }
        }

        return false;
    }


    private boolean checkFreezeDeadlock(TileInfo crate) {
        return checkAxisFreezeDeadlock(crate, Direction.LEFT) &&
                checkAxisFreezeDeadlock(crate, Direction.UP);
    }

    private boolean checkAxisFreezeDeadlock(TileInfo current, Direction axis) {
        boolean deadlock = false;

        TileInfo left = current.safeAdjacent(axis);
        TileInfo right = current.safeAdjacent(axis.negate());

        if ((left != null && left.isWall()) || (right != null && right.isWall())) { // rule 1
            deadlock = true;

        } else if ((left == null || left.isDeadTile()) &&
                (right == null || right.isDeadTile())) { // rule 2

            deadlock = true;
        } else { // rule 3
            Tile oldCurr = current.getTile();
            current.setTile(Tile.WALL);

            if (left != null && left.anyCrate()) {
                deadlock = checkFreezeDeadlock(left);
            }

            if (!deadlock && right != null && right.anyCrate()) {
                deadlock = checkFreezeDeadlock(right);
            }

            current.setTile(oldCurr);
        }

        // ultimate check, the crate is frozen if it is only a crate and not a crate on target
        return deadlock;
    }



    protected boolean checkPICorralDeadlock(Board board, State state) {
        CorralDetector detector = board.getCorralDetector();
        detector.findPICorral(board, state.cratesIndices());

        for (Corral corral : detector.getCorrals()) {
            if (corral.isPICorral()) {
                if (checkPICorralDeadlock(board, state, corral)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean checkPICorralDeadlock(Board board, State state, Corral corral) {
        // int statesExplored = 0;

        // remove crates
        for (int crate : state.cratesIndices()) {
            if (!isInCorral(board, crate, corral)) {
                board.getAt(crate).removeCrate();;
            }
        }

        boolean deadlock = true;

        for (TileInfo crate : corral.barrier) {
            for (Direction dir : Direction.VALUES) {
                TileInfo player = crate.adjacent(dir.negate());

                if (player.isReachable()) {
                    TileInfo crateDest = crate.adjacent(dir);

                    if (crateDest.isSolid()) {
                        continue;
                    }

                    crateDest.addCrate();
                    crate.removeCrate();

                    if (!checkFreezeDeadlock(crateDest)) {
                        deadlock = false;
                    }

                    crateDest.removeCrate();
                    crate.addCrate();
                }
            }
        }


        // re add crates
        for (int crate : state.cratesIndices()) {
            if (!isInCorral(board, crate, corral)) {
                board.getAt(crate).addCrate();;
            }
        }

        return deadlock;
    }

    private boolean isInCorral(Board board, int crate, Corral corral) {
        TileInfo tile = board.getAt(crate);

        List<Corral> adjacentCorrals = tile.getAdjacentCorrals();
        for (int i = 0; i < adjacentCorrals.size(); i++) {
            Corral adj = adjacentCorrals.get(i);

            if (adj == corral) {
                return true;
            }
        }

        return false;
    }


    @Override
    public List<SolverParameter> getParameters() {
        List<SolverParameter> params = new ArrayList<>();
        addParameters(params);
        return params;
    }

    /**
     * Add your parameters to the list returned by {@link #getParameters()}
     * @param parameters parameters that will be returned by {@link #getParameters()}
     */
    protected void addParameters(List<SolverParameter> parameters) {

    }
}
