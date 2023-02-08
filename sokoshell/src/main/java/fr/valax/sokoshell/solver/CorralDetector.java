package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

/**
 * A union find structure to find corral in a map.
 * The objective of this object is to compute corral,
 * barriers and topY, topX position of each corral.
 */
public class CorralDetector {

    private final Corral[] corrals;
    private final int[] parent;
    private final int[] rank;

    public CorralDetector(int size) {
        parent = new int[size];
        rank = new int[size];
        corrals = new Corral[size];

        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
            corrals[i] = new Corral();
        }
    }

    public void findCorral(Board board, int playerX, int playerY) {
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

                        if (left.anyCrate()) {
                            corral.crates.add(left);
                        }
                    } else if (!left.isSolid()) {
                        corral = addToCorral(t, left);

                        if (left.anyCrate()) {
                            corral.crates.add(left);
                        }
                    } else {
                        corral = newCorral(t);

                        if (up.anyCrate()) {
                            corral.crates.add(up);
                        }

                        if (left.anyCrate()) {
                            corral.crates.add(left);
                        }
                    }

                    TileInfo down = board.getAt(x, y + 1);
                    TileInfo right = board.getAt(x + 1, y);
                    if (down.anyCrate()) {
                        corral.crates.add(down);
                    }
                    if (right.anyCrate()) {
                        corral.crates.add(right);
                    }

                    if (t.isAt(playerX, playerY)) {
                        corral.containsPlayer = true;
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
     * @return the corral in which was added the tile
     */
    private Corral addToCorral(TileInfo tile, TileInfo inCorral) {
        int i = tile.getIndex();
        int rootI = find(inCorral.getIndex());

        parent[i] = rootI;
        rank[i] = 0;

        Corral corral = corrals[rootI];
        corral.tiles.add(tile);

        return corral;
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
        corral.tiles.clear();
        corral.crates.clear();
        corral.containsPlayer = false;
        corral.tiles.add(tile);

        return corral;
    }

    private Corral mergeTwoCorrals(TileInfo inCorral1, TileInfo inCorral2) {
        int corral1I = find(inCorral1.getIndex());
        int corral2I = find(inCorral2.getIndex());

        if (corral1I != corral2I) {
            Corral corral1 = corrals[corral1I];
            Corral corral2 = corrals[corral2I];

            if (rank[corral1I] < rank[corral2I]) {
                mergeInto(corral1, corral2);
                parent[corral1I] = corral2I;
                return corrals[corral2I];
            } else if (rank[corral1I] > rank[corral2I]) {
                mergeInto(corral2, corral1);
                parent[corral2I] = corral1I;
                return corrals[corral1I];
            } else {
                mergeInto(corral1, corral2);
                parent[corral1I] = corral2I;
                rank[corral2I]++;
                return corrals[corral2I];
            }
        }

        return corrals[corral1I];
    }

    private void mergeInto(Corral old, Corral new_) {
        new_.tiles.addAll(old.tiles);
        new_.crates.addAll(old.crates);
        new_.containsPlayer = old.containsPlayer() | new_.containsPlayer();
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
}
