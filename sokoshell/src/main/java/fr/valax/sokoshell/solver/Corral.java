package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.mark.FixedSizeMarkSystem;
import fr.valax.sokoshell.solver.board.mark.MarkSystem;
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
    protected boolean onlyCrateOnTarget; // true if all crates in crates are crate on target


    protected final Set<State> visited = new HashSet<>();
    protected final Queue<State> toVisit = new ArrayDeque<>();
    protected final FixedSizeMarkSystem reachable;

    public Corral(int id, Board board) {
        this.id = id;
        this.board = board;
        this.reachable = new FixedSizeMarkSystem(board.getWidth() * board.getHeight());
    }

    public boolean isDeadlock(State originalState) {
        if (!isPICorral || onlyCrateOnTarget) {
            return false;
        }

        boolean deadlock = true;
        State firstState = removeOutsideCrate(originalState);

        visited.add(firstState);
        toVisit.add(firstState);

        while (!toVisit.isEmpty() && visited.size() < 1000 && deadlock) {
            State s = toVisit.remove();

            board.addStateCrates(s);

            if (FreezeDeadlockDetector.checkFreezeDeadlock(board, s)) {
                board.removeStateCrates(s);
                continue;
            }

            findReachableCases(board.getAt(s.playerPos()));
            deadlock = addChildrenStates(s);

            board.removeStateCrates(s);
        }

        visited.clear();
        toVisit.clear();

        // re-add crates
        board.addStateCrates(originalState);

        return deadlock;
    }

    /**
     * @param state current state
     * @return true if not a deadlock
     */
    private boolean addChildrenStates(State state) {
        int[] cratesIndices = state.cratesIndices();

        int numOnTarget = 0;
        for (int crate : cratesIndices) {
            if (board.getAt(crate).isCrateOnTarget()) {
                numOnTarget++;
            }
        }


        for (int i = 0; i < cratesIndices.length; i++) {
            TileInfo crate = board.getAt(cratesIndices[i]);

            for (Direction dir : Direction.VALUES) {
                TileInfo player = crate.adjacent(dir.negate());

                // also checks if tile is solid: a solid tile is never reachable
                if (!reachable.isMarked(player.getIndex())) {
                    continue;
                }

                TileInfo dest = crate.adjacent(dir);
                if (dest.isSolid() || dest.isDeadTile()) {
                    continue;
                }

                // a crate can be moved outside the corral
                if (!isInCorral(dest)) {
                    return false;
                }

                // all crates of the corral can be moved to a target
                if (dest.isTarget() && numOnTarget == cratesIndices.length - 1) {
                    return false;
                }

                // create sub state
                int newPlayerPos = board.topLeftReachablePosition(crate, dest);
                State sub = state.child(newPlayerPos, i, dest.getIndex());

                if (visited.add(sub)) {
                    toVisit.offer(sub);
                }
            }
        }

        return true;
    }

    private void findReachableCases(TileInfo tile) {
        reachable.unmarkAll();
        findReachableCases_aux(tile);
    }

    private void findReachableCases_aux(TileInfo tile) {
        reachable.mark(tile.getIndex());
        for (Direction d : Direction.VALUES) {
            TileInfo adjacent = tile.adjacent(d);

            // the second part of the condition avoids to check already processed cases
            if (!adjacent.isSolid() && !reachable.isMarked(adjacent.getIndex())) {
                findReachableCases_aux(adjacent);
            }
        }
    }

    /**
     * Remove crates that are not part of the corral
     * and create a new state without these crates
     * @param state current state
     * @return a state without crate outside the corral
     */
    private State removeOutsideCrate(State state) {
        int[] newCrates = new int[crates.size()];
        int[] oldCrates = state.cratesIndices();
        int j = 0;
        for (int i = 0; i < oldCrates.length; i++) {
            if (isInCorral(oldCrates[i])) {
                newCrates[j] = oldCrates[i];
                j++;
            } else {
                board.getAt(oldCrates[i]).removeCrate();
            }
        }


        return new State(state.playerPos(), newCrates, null);
    }

    private boolean isInCorral(int crate) {
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

    private boolean isInCorral(TileInfo tile) {
        Corral c = board.getCorral(tile);

        if (c == null) {
            return isInCorral(tile.getIndex());
        } else {
            return c == this;
        }
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
