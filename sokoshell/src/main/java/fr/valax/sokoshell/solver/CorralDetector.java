package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A union find structure to find corral in a map.
 * The objective of this object is to compute corral,
 * barriers and topY, topX position of each corral.
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class CorralDetector {

    private final Corral[] corrals;
    private final int[] parent;
    private final int[] rank;

    private final Set<Corral> currentCorrals;

    public CorralDetector(int size) {
        parent = new int[size];
        rank = new int[size];
        corrals = new Corral[size];

        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
            corrals[i] = new Corral(i);
        }

        currentCorrals = new HashSet<>(size);
    }

    /**
     * Find corral. Compute topX, topY. Find the corral that
     * contains the player.
     * Other values (isPICorral, crates, barriers) are not
     * valid after a call to this method. Use {@link #findPICorral(Board, int[])}
     * to revalidate them.
     *
     * @param board the board
     * @param playerX player position x
     * @param playerY player position y
     */
    public void findCorral(Board board, int playerX, int playerY) {
        currentCorrals.clear();

        int h = board.getHeight();
        int w = board.getWidth();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                TileInfo t = board.getAt(x, y);

                if (!t.isSolid()) {
                    TileInfo up = board.getAt(x, y - 1);
                    TileInfo left = board.getAt(x - 1, y);

                    Corral corral;
                    if (!up.isSolid() && !left.isSolid()) {
                        addToCorral(t, up);
                        corral = mergeTwoCorrals(up, left);
                    } else if (!up.isSolid()) {
                        corral = addToCorral(t, up);
                    } else if (!left.isSolid()) {
                        corral = addToCorral(t, left);
                    } else {
                        corral = newCorral(t);
                    }

                    if (t.isAt(playerX, playerY)) {
                        corral.containsPlayer = true;
                    }
                }
            }
        }
    }

    /**
     * Find PI corral
     * @param board the board
     * @param crates crates on the board
     */
    public void findPICorral(Board board, int[] crates) {
        // compute adjacent corrals of crates, barriers
        // this step can also reduce the number of false PI corral
        for (int crateI : crates) {
            TileInfo crate = board.getAt(crateI);

            List<Corral> adj = crate.getAdjacentCorrals();
            adj.clear();

            // find adjacent corrals
            boolean playerCorralAdjacent = false;
            for (Direction dir : Direction.VALUES) {
                TileInfo tile = crate.adjacent(dir);
                if (tile.isSolid()) {
                    continue;
                }

                Corral corral = findCorral(tile);
                // maximal size of adj is 4, so I think that using a list rather than a set is faster
                if (!adj.contains(corral)) {
                    adj.add(corral);
                }

                if (corral.containsPlayer()) {
                    playerCorralAdjacent = true;
                }
            }

            if (adj.size() == 1) {
                // the crate is inside a corral
                // and not a part of a barrier
                adj.get(0).crates.add(crate);
            } else if (adj.size() > 1) {
                // crate is a part of a barrier
                for (int i = 0; i < adj.size(); i++) {
                    adj.get(i).crates.add(crate);
                    adj.get(i).barrier.add(crate);
                }

                // reduce number of false PI corral.
                // PI corral must have for unique corral neighbor
                // the 'player corral'. Therefore, if the crate
                // has more than 3 adjacent corrals or if the
                // adjacent corrals doesn't contain the player,
                // all the adjacent corral cannot be PI corral
                if (adj.size() >= 3 || !playerCorralAdjacent) {
                    // either too many corrals are linked, either the player isn't involved
                    for (int i = 0; i < adj.size(); i++) {
                        adj.get(i).isPICorral = false;
                    }
                }
            }
        }

        // finish computing pi corral property
        for (int i = 0; i < corrals.length; i++) {
            Corral c = corrals[i];

            if (c.containsPlayer() || !c.isPICorral()) {
                c.isPICorral = false;
                continue;
            }

            // at this step, we know that c has for unique
            // corral neighbor the 'player corral'.
            // We only need to check that every crate in the barrier
            // can be pushed inside the corral.
            List<TileInfo> barrier = c.barrier;
            for (int j = 0; j < barrier.size(); j++) {
                TileInfo crate = barrier.get(j);
                if (!canBePushedInside(crate, Direction.LEFT) && !canBePushedInside(crate, Direction.UP)) {
                    c.isPICorral = false;
                    break;
                }
            }
        }
    }

    /**
     * This method assume that adjacent tiles of {@code crate}
     * are player-reachable.
     *
     * @param crate the crate
     * @param axis axis
     * @return true if {@code crate} can be pushed on the given axis
     */
    private boolean canBePushedInside(TileInfo crate, Direction axis) {
        TileInfo front = crate.adjacent(axis);
        if (front.isSolid()) {
            return false;
        }

        TileInfo back = crate.adjacent(axis.negate());
        return !back.isSolid();
    }

    /**
     * Move a node from a tree to another. {@code node}
     * and {@code dest} must be in separate trees.
     * This method breaks the union find structure.
     * So, it must be used carefully.
     * @return the corral in which was added the tile
     */
    private Corral addToCorral(TileInfo tile, TileInfo inCorral) {
        int i = tile.getIndex();
        int rootI = find(inCorral.getIndex());

        parent[i] = rootI;
        rank[i] = 0;

        return corrals[rootI];
    }

    /**
     * Remove a node from his tree and create a new tree.
     * This method breaks the union find structure.
     * So, it must be used carefully.
     * @return the new corral
     */
    private Corral newCorral(TileInfo tile) {
        int i = tile.getIndex();
        parent[i] = i;
        rank[i] = 0;

        Corral corral = corrals[i];
        corral.containsPlayer = false;
        corral.isPICorral = true;
        corral.crates.clear();
        corral.barrier.clear();
        corral.topX = tile.getX();
        corral.topY = tile.getY();

        currentCorrals.add(corral);

        return corral;
    }

    private void removeCorral(TileInfo tile) {
        int i = tile.getIndex();
        parent[i] = i;
        rank[i] = 0;
    }

    private Corral mergeTwoCorrals(TileInfo inCorral1, TileInfo inCorral2) {
        int corral1I = find(inCorral1.getIndex());
        int corral2I = find(inCorral2.getIndex());

        if (corral1I != corral2I) {
            int oldCorralI;
            int newCorralI;
            if (rank[corral1I] < rank[corral2I]) {
                oldCorralI = corral1I;
                newCorralI = corral2I;
            } else if (rank[corral1I] > rank[corral2I]) {
                oldCorralI = corral2I;
                newCorralI = corral1I;
            } else {
                oldCorralI = corral1I;
                newCorralI = corral2I;
                rank[newCorralI]++;
            }

            parent[oldCorralI] = newCorralI;

            Corral newCorral = corrals[newCorralI];
            Corral oldCorral = corrals[oldCorralI];

            currentCorrals.remove(oldCorral);
            newCorral.containsPlayer |= oldCorral.containsPlayer();

            if (oldCorral.topY < newCorral.topY || (oldCorral.topY == newCorral.topY && oldCorral.topX < newCorral.topX)) {
                newCorral.topX = oldCorral.topX;
                newCorral.topY = oldCorral.topY;
            }

            return newCorral;
        }

        return corrals[corral1I];
    }


    private int find(int i) {
        if (parent[i] != i) {
            int root = find(parent[i]);
            parent[i] = root;

            return root;
        }

        return i;
    }

    /**
     * The tile must be a non-solid tile: a floor or a target
     * @param tile a floor or target tile
     * @return the corral in which the tile is
     */
    public Corral findCorral(TileInfo tile) {
        return corrals[find(tile.getIndex())];
    }

    public Set<Corral> getCorrals() {
        return currentCorrals;
    }
}
