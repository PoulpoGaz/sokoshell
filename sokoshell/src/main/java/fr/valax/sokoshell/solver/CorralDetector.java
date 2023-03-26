package fr.valax.sokoshell.solver;

import fr.valax.args.utils.ArgsUtils;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.*;

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

    private int realNumberOfCorral;

    public CorralDetector(Board board) {
        int size = board.getWidth() * board.getHeight();
        parent = new int[size];
        rank = new int[size];
        corrals = new Corral[size];

        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
            corrals[i] = new Corral(i, board);
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
            TileInfo left = board.getAt(0, y);

            for (int x = 1; x < w - 1; x++) {
                TileInfo t = board.getAt(x, y);

                if (!t.isSolid()) {
                    TileInfo up = board.getAt(x, y - 1);

                    if (!up.isSolid() && !left.isSolid()) {
                        addToCorral(t, up);
                        mergeTwoCorrals(up, left);
                    } else if (!up.isSolid()) {
                        addToCorral(t, up);
                    } else if (!left.isSolid()) {
                        addToCorral(t, left);
                    } else {
                        newCorral(t);
                    }
                } else {
                    int i = t.getIndex();
                    parent[i] = -1;
                    rank[i] = -1;
                    corrals[i].isValid = false;
                }

                left = t;
            }
        }

        int playerCorral = find(playerY * board.getWidth() + playerX);
        corrals[playerCorral].containsPlayer = true;

        realNumberOfCorral = currentCorrals.size();
    }

    /**
     * Find PI corral
     * @param board the board
     * @param crates crates on the board
     */
    public void findPICorral(Board board, int[] crates) {
        preComputePICorral(board, crates);

        List<Corral> corrals = new ArrayList<>(currentCorrals);

        for (int i = 0; i < corrals.size(); i++) {
            Corral c = corrals.get(i);

            if (!c.containsPlayer()) {
                if (isPICorral(c)) {
                    c.isPICorral = Corral.IS_A_PI_CORRAL;
                    corrals.remove(i);
                    i--;
                }
            } else {
                c.isPICorral = Corral.NOT_A_PI_CORRAL;
                corrals.remove(i);
                i--;
            }
        }

        for (Corral c : corrals) {
            if (c.isValid && c.isPICorral == Corral.POTENTIAL_PI_CORRAL) {
                mergeWithAdjacents(board, c);
            }
        }
    }

    protected boolean isICorral(Corral corral) {
        for (TileInfo crate : corral.barrier) {
            for (Direction dir : Direction.VALUES) {
                TileInfo crateDest = crate.adjacent(dir);
                if (crateDest.isSolid()) {
                    continue;
                }

                TileInfo player = crate.adjacent(dir.negate());
                if (player.isSolid()) {
                    continue;
                }

                Corral corralDest = findCorral(crateDest);
                Corral playerCorral = findCorral(player);

                if (corralDest == playerCorral) {
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean isPICorral(Corral corral) {
        if (!corral.adjacentToPlayerCorral || corral.adjacentCorrals.size() != 1) {
            return false;
        }

        for (TileInfo crate : corral.barrier) {
            for (Direction dir : Direction.VALUES) {
                TileInfo crateDest = crate.adjacent(dir);
                if (crateDest.isSolid()) {
                    continue;
                }

                TileInfo player = crate.adjacent(dir.negate());
                if (player.isWall()) {
                    continue;
                } else if (player.anyCrate()) {
                    /*if (!corral.crates.contains(player) && !corral.barrier.contains(player)) {
                        return false;
                    }*/
                    continue;
                }

                if (crateDest.isDeadTile()) {
                    continue; // only consider valid moves
                }

                Corral corralDest = findCorral(crateDest);
                Corral playerCorral = findCorral(player);

                if (playerCorral.containsPlayer() && playerCorral == corralDest) {
                    return false;
                }
            }
        }

        return true;
    }

    protected void mergeWithAdjacents(Board board, Corral corral) {
        while (corral.adjacentCorrals.size() > 1) {
            Iterator<Corral> iterator = corral.adjacentCorrals.iterator();
            Corral adj = null;

            while (iterator.hasNext()) {
                adj = iterator.next();

                if (adj.isPICorral()) {
                    return;
                }

                if (!adj.containsPlayer) {
                    break;
                }
            }

            corral = fullyMergeTwoCorrals(board, corral, adj);
        }

        if (isPICorral(corral)) {
            corral.isPICorral = Corral.IS_A_PI_CORRAL;
        } else {
            corral.isPICorral = Corral.NOT_A_PI_CORRAL;
        }
    }

    private Corral fullyMergeTwoCorrals(Board board, Corral a, Corral b) {
        Corral corral = mergeTwoCorrals(board.getAt(a.getTopX(), a.getTopY()), board.getAt(b.getTopX(), b.getTopY()));

        if (corral == b) {
            b = a; // this way, we can deal with corral (before a) and b, without doing disjonction.
        }

        // Merge properties. It is assumed that a and b doesn't contain the player
        // topX, topY are already updated
        // the set currentCorrals was also updated.
        corral.adjacentToPlayerCorral |= b.adjacentToPlayerCorral;
        corral.onlyCrateOnTarget &= b.onlyCrateOnTarget;

        // update adjacentCorrals
        // Add all adjacents corral of b to corral, but corral is adjacent to b,
        // we must remove it. The remove is done before addAll because the resulting
        // set is likely to be bigger than b one.
        b.adjacentCorrals.remove(corral);
        // also update adjacent of b
        for (Corral bAdj : b.adjacentCorrals) {
            bAdj.adjacentCorrals.remove(b);

            if (bAdj != corral) {
                bAdj.adjacentCorrals.add(corral);
            }
        }
        corral.adjacentCorrals.remove(b);
        corral.adjacentCorrals.addAll(b.adjacentCorrals);

        // update barrier and crates
        for (TileInfo tile : b.crates) {
            if (!corral.crates.contains(tile)) {
                corral.crates.add(tile);
            }
        }

        // merge the two barrier. Some crates aren't in a barrier.
        for (TileInfo tile : b.barrier) {
            if (!corral.barrier.contains(tile)) {
                corral.barrier.add(tile);
            }
        }


        int[] adjacents = new int[4];
        int size;
        for (int i = 0; i < corral.barrier.size(); i++) {
            TileInfo crate = corral.barrier.get(i);
            size = 0;
            for (Direction dir : Direction.VALUES) {
                TileInfo tile = crate.adjacent(dir);
                if (tile.isSolid()) {
                    continue;
                }

                Corral adj = findCorral(tile);

                boolean new_ = true;
                for (int k = 0; k < size; k++) {
                    if (adjacents[k] == adj.id) {
                        new_ = false;
                        break;
                    }
                }

                if (new_) {
                    adjacents[size] = adj.id;
                    size++;
                }
            }

            if (size <= 1) { // not in barrier !
                corral.barrier.remove(i);
                i--;
            }
        }

        return corral;
    }

    /**
     * Compute adjacent corrals of crates, barriers and various property of Corral
     */
    protected void preComputePICorral(Board board, int[] crates) {
        List<Corral> adj = new ArrayList<>();

        for (int crateI : crates) {
            TileInfo crate = board.getAt(crateI);

            adj.clear();

            // find adjacent corrals
            boolean adjacentToPlayerCorral = false;
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
                    adjacentToPlayerCorral = true;
                }
            }

            if (adj.size() == 1) {
                // the crate is inside a corral
                // and not a part of a barrier
                adj.get(0).crates.add(crate);

                if (crate.isCrate()) {
                    adj.get(0).onlyCrateOnTarget = false;
                }
            } else if (adj.size() > 1) {
                // crate is a part of a barrier
                for (int i = 0; i < adj.size(); i++) {
                    Corral corral = adj.get(i);
                    corral.crates.add(crate);
                    corral.barrier.add(crate);
                    corral.adjacentToPlayerCorral |= adjacentToPlayerCorral;

                    if (crate.isCrate()) {
                        corral.onlyCrateOnTarget = false;
                    }

                    for (int j = i + 1; j < adj.size(); j++) {
                        Corral corral2 = adj.get(j);

                        if (corral.adjacentCorrals.add(corral2)) {
                            corral2.adjacentCorrals.add(corral);
                        }
                    }
                }
            }
        }
    }

    /**
     * Move a node from a tree to another. {@code node}
     * and {@code dest} must be in separate trees.
     * This method breaks the union find structure.
     * So, it must be used carefully.
     */
    private void addToCorral(TileInfo tile, TileInfo inCorral) {
        int i = tile.getIndex();
        int rootI = find(inCorral.getIndex());

        parent[i] = rootI;
        rank[i] = 0;
    }

    /**
     * Remove a node from his tree and create a new tree.
     * This method breaks the union find structure.
     * So, it must be used carefully.
     */
    private void newCorral(TileInfo tile) {
        int i = tile.getIndex();
        parent[i] = i;
        rank[i] = 0;

        Corral corral = corrals[i];
        corral.containsPlayer = false;
        corral.isPICorral = Corral.POTENTIAL_PI_CORRAL;
        corral.onlyCrateOnTarget = true;
        corral.isValid = true;
        corral.crates.clear();
        corral.barrier.clear();
        corral.adjacentCorrals.clear();
        corral.topX = tile.getX();
        corral.topY = tile.getY();

        currentCorrals.add(corral);
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

            oldCorral.isValid = false;
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
        int i = tile.getIndex();

        if (parent[i] < 0) {
            return null;
        }

        return corrals[find(i)];
    }

    public Collection<Corral> getCorrals() {
        return currentCorrals;
    }

    public int getRealNumberOfCorral() {
        return realNumberOfCorral;
    }

    public void setDeadlockTable(DeadlockTable table) {
        for (Corral c : corrals) {
            c.setDeadlockTable(table);
        }
    }
}
