package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.*;

public class Corral {

    protected final int id;
    protected final Board board;

    protected int topX;
    protected int topY;

    /**
     * All crates that inside the corral or surrounding the corral
     */
    protected final List<TileInfo> barrier = new ArrayList<>();
    protected final List<TileInfo> crates = new ArrayList<>();
    protected boolean containsPlayer;
    protected boolean isPICorral;

    protected final Set<State> visited = new HashSet<>();
    protected final Queue<State> toVisit = new ArrayDeque<>();

    public Corral(int id, Board board) {
        this.id = id;
        this.board = board;
    }

    public boolean isDeadlock(State state) {
        if (!isPICorral) {
            return false;
        }

        // remove crates
        for (int crate : state.cratesIndices()) {
            if (!isInCorral(board, crate)) {
                board.getAt(crate).removeCrate();;
            }
        }

        boolean deadlock = true;

        for (TileInfo crate : barrier) {
            for (Direction dir : Direction.VALUES) {
                TileInfo player = crate.adjacent(dir.negate());

                if (player.isReachable()) {
                    TileInfo crateDest = crate.adjacent(dir);

                    if (crateDest.isSolid()) {
                        continue;
                    }

                    crateDest.addCrate();
                    crate.removeCrate();

                    if (!FreezeDeadlockDetector.checkFreezeDeadlock(crateDest)) {
                        deadlock = false;
                    }

                    crateDest.removeCrate();
                    crate.addCrate();
                }
            }
        }

        // re-add crates
        for (int crate : state.cratesIndices()) {
            if (!isInCorral(board, crate)) {
                board.getAt(crate).addCrate();;
            }
        }

        return deadlock;
    }

    private boolean isInCorral(Board board, int crate) {
        TileInfo tile = board.getAt(crate);

        List<Corral> adjacentCorrals = tile.getAdjacentCorrals();
        for (int i = 0; i < adjacentCorrals.size(); i++) {
            Corral adj = adjacentCorrals.get(i);

            if (adj == this) {
                return true;
            }
        }

        return false;
    }

    public int getTopX() {
        return topX;
    }

    public int getTopY() {
        return topY;
    }

    public List<TileInfo> getBarrier() {
        return barrier;
    }

    public List<TileInfo> getCrates() {
        return crates;
    }

    public boolean containsPlayer() {
        return containsPlayer;
    }

    public boolean isPICorral() {
        return isPICorral;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Corral corral)) return false;

        return id == corral.id;
    }
}
