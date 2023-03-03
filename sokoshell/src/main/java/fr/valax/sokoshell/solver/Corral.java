package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.Tunnel;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.*;

public class Corral {

    public static final int POTENTIAL_PI_CORRAL = 0;
    public static final int IS_A_PI_CORRAL = 1;
    public static final int NOT_A_PI_CORRAL = 2;

    protected final int id;
    protected final Board board;

    protected int topX;
    protected int topY;

    protected final Set<Corral> adjacentCorrals = new HashSet<>();

    /**
     * All crates that are inside the corral and surrounding the corral
     */
    protected final List<TileInfo> barrier = new ArrayList<>();
    protected final List<TileInfo> crates = new ArrayList<>();
    protected boolean containsPlayer;
    protected boolean adjacentToPlayerCorral; // the player corral is adjacent to itself
    protected int isPICorral;
    protected boolean onlyCrateOnTarget; // true if all crates in crates list are crate on target
    protected boolean isValid = false;


    protected final Set<CorralState> visited = new HashSet<>();
    protected final Queue<CorralState> toVisit = new ArrayDeque<>();
    protected final ReachableTiles reachable;
    private CorralState currentState;

    public Corral(int id, Board board) {
        this.id = id;
        this.board = board;
        this.reachable = new ReachableTiles(board);
    }

    public boolean isDeadlock(State originalState) {
        if (!isPICorral() || onlyCrateOnTarget || crates.size() == originalState.cratesIndices().length) {
            return false;
        }

        addFrozenCrates(originalState);
        if (crates.size() == originalState.cratesIndices().length) {
            return false;
        }

        boolean deadlock = true;
        CorralState firstState = removeOutsideCrate(originalState);

        visited.add(firstState);
        toVisit.add(firstState);

        while (!toVisit.isEmpty() && deadlock) {
            currentState = toVisit.remove();

            board.addStateCrates(currentState);

            if (FreezeDeadlockDetector.checkFreezeDeadlock(board, currentState)) {
                board.removeStateCrates(currentState);
                continue;
            }

            board.computeTunnelStatus(currentState);
            reachable.findReachableCases(board.getAt(currentState.playerPos()));
            deadlock = addChildrenStates();

            board.removeStateCrates(currentState);

            if (visited.size() >= 1000) {
                deadlock = false;
            }
        }

        visited.clear();
        toVisit.clear();

        // re-add crates
        board.addStateCrates(originalState);

        return deadlock;
    }

    private void addFrozenCrates(State state) {
        for (int i : state.cratesIndices) {
            TileInfo crate = board.getAt(i);

            if (crates.contains(crate)) {
                continue;
            }

            if (isFrozen(crate, Direction.LEFT) && isFrozen(crate, Direction.UP)) {
                crates.add(crate);
            }
        }
    }

    /**
     * True if the crate is almost frozen ie right now it can be moved
     * in the axis: it happens when an adjacent tile on the axis is solid.
     * The adjacent tile must be in the corral is it is a crate
     */
    private boolean isFrozen(TileInfo tile, Direction axis) {
        TileInfo left = tile.adjacent(axis);
        TileInfo right = tile.adjacent(axis.negate());

        return left.isWall() ||
                left.anyCrate() && crates.contains(left) ||
                right.isWall() ||
                right.anyCrate() && crates.contains(right);
    }


    /**
     * @return false if not a deadlock
     */
    private boolean addChildrenStates() {
        int[] cratesIndices = currentState.cratesIndices();

        boolean deadlock = true;
        for (int i = 0; i < cratesIndices.length && deadlock; i++) {
            TileInfo crate = board.getAt(cratesIndices[i]);

            if (crate.isInATunnel()) {
                deadlock = addChildrenStatesInTunnel(i, crate);
            } else {
                deadlock = addChildrenStatesDefault(i, crate);
            }
        }

        return deadlock;
    }

    //
    // THE TWO FOLLOWING METHODS ARE COPIED FROM ABSTRACT SOLVER.
    // I hope that one day, I will change that
    //

    protected boolean addChildrenStatesInTunnel(int crateIndex, TileInfo crate) {
        // the crate is in a tunnel. two possibilities: move to tunnel.startOut or tunnel.endOut
        // this part of the code assume that there is no other crate in the tunnel.
        // normally, this is impossible...

        for (Direction pushDir : Direction.VALUES) {
            TileInfo player = crate.adjacent(pushDir.negate());

            if (reachable.isReachable(player)) {
                TileInfo dest = crate.getTunnelExit().getExit(pushDir);

                if (dest != null && !dest.isSolid()) {
                    if (!addState(crateIndex, crate, dest, pushDir)) {
                        return false; // not a deadlock
                    }
                }
            }
        }

        return true;
    }

    protected boolean addChildrenStatesDefault(int crateIndex, TileInfo crate) {
        for (Direction d : Direction.VALUES) {
            TileInfo crateDest = crate.adjacent(d);
            if (crateDest.isSolid()) {
                continue; // The destination case is not empty
            }

            if (crateDest.isDeadTile()) {
                continue; // Useless to push a crate on a dead position
            }

            TileInfo player = crate.adjacent(d.negate());
            if (!reachable.isReachable(player)) {
                // The player cannot reach the case to push the crate
                // also checks if tile is solid: a solid tile is never reachable
                continue;
            }


            // check for tunnel
            Tunnel tunnel = crateDest.getTunnel();

            // the crate will be pushed inside the tunnel
            if (tunnel != null) {
                if (tunnel.crateInside()) { // pushing inside will lead to a corral deadlock
                    continue;
                }

                // ie the crate can't be pushed to the other extremities of the tunnel
                // however, sometimes (boxxle 24) it is useful to push the crate inside
                // the tunnel. That's why the second addState is done (after this if)
                // and only if this tunnel isn't oneway
                if (!tunnel.isPlayerOnlyTunnel()) {
                    TileInfo newDest = null;
                    Direction pushDir = null;

                    if (crate == tunnel.getStartOut()) {
                        if (tunnel.getEndOut() != null && !tunnel.getEndOut().anyCrate()) {
                            newDest = tunnel.getEndOut();
                            pushDir = tunnel.getEnd().direction(tunnel.getEndOut());
                        }
                    } else {
                        if (tunnel.getStartOut() != null && !tunnel.getStartOut().anyCrate()) {
                            newDest = tunnel.getStartOut();
                            pushDir = tunnel.getStart().direction(tunnel.getStartOut());
                        }
                    }

                    if (newDest != null && !newDest.isDeadTile()) {
                        if (!addState(crateIndex, crate, newDest, pushDir)) {
                            return false;
                        }
                    }
                }

                if (tunnel.isOneway()) {
                    continue;
                }
            }

            if (!addState(crateIndex, crate, crateDest, d)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return false if not a deadlock
     */
    private boolean addState(int crateIndex, TileInfo crate, TileInfo dest, Direction pushDir) {
        // a crate can be moved outside the corral
        if (!isInCorral(dest)) {
            return false;
        }

        // all crates of the corral can be moved to a target

        int n = 0;
        for (int i : currentState.cratesIndices()) {
            if (i != crate.getIndex() && board.getAt(i).isCrateOnTarget()) {
                n++;
            }
        }

        if (dest.isTarget() && n + 1 == currentState.cratesIndices.length) { // TODO: crate may be on target
            return false;
        }

        // create sub state
        int newPlayerPos = board.topLeftReachablePosition(crate, dest);
        CorralState sub = currentState.child(newPlayerPos, crateIndex, dest.getIndex());

        if (crate.isCrate() && dest.isTarget()) {
            sub.increaseNumberOnTarget();
        } else if (crate.isCrateOnTarget() && dest.isFloor()) {
            sub.decreaseNumberOnTarget();
        }

        if (visited.add(sub)) {
            toVisit.offer(sub);
        }

        return true;
    }

    /**
     * Remove crates that are not part of the corral
     * and create a new state without these crates
     * @param state current state
     * @return a state without crate outside the corral
     */
    private CorralState removeOutsideCrate(State state) {
        int numOnTarget = 0;

        int[] newCrates = new int[crates.size()];
        int[] oldCrates = state.cratesIndices();
        int j = 0;
        for (int i = 0; i < oldCrates.length; i++) {
            TileInfo crate = board.getAt(oldCrates[i]);
            if (isInCorral(oldCrates[i])) {
                if (crate.isCrateOnTarget()) {
                    numOnTarget++;
                }

                newCrates[j] = oldCrates[i];
                j++;
            } else {
                crate.removeCrate();
            }
        }

        CorralState corralState = new CorralState(state.playerPos(), newCrates, null);
        corralState.setNumOnTarget(numOnTarget);
        return corralState;
    }

    private boolean isInCorral(int crate) {
        TileInfo tile = board.getAt(crate);

        return crates.contains(tile);
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
        return isPICorral == IS_A_PI_CORRAL;
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

    private static class CorralState extends State {

        private int numOnTarget;

        public CorralState(int playerPos, int[] cratesIndices, State parent) {
            super(playerPos, cratesIndices, parent);
        }

        public CorralState(int playerPos, int[] cratesIndices, int hash, State parent) {
            super(playerPos, cratesIndices, hash, parent);
        }

        private CorralState(State state) {
            super(state.playerPos, state.cratesIndices, state.hash, state.parent);
        }

        @Override
        public CorralState child(int newPlayerPos, int crateToMove, int crateDestination) {
            return new CorralState(super.child(newPlayerPos, crateToMove, crateDestination));
        }

        public void increaseNumberOnTarget() {
            numOnTarget++;
        }

        public void decreaseNumberOnTarget() {
            numOnTarget--;
        }

        public int getNumOnTarget() {
            return numOnTarget;
        }

        public void setNumOnTarget(int numOnTarget) {
            this.numOnTarget = numOnTarget;
        }
    }
}
