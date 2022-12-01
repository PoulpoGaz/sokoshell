package fr.valax.sokoshell.solver;

import java.util.*;

public class Pathfinder {

    public static boolean hasPath(TileInfo player, TileInfo playerDest, TileInfo crate, TileInfo crateDest) {
        return findPath(player, playerDest, crate, crateDest) != null;
    }

    /**
     * Find a path between playerStart and playerDest and optionally crateStart and crateDest.
     * The player is only allowed to move one crate
     *
     * @param playerStart player start position
     * @param playerDest player destination
     * @param crateStart optional crate start position. If not {@code null} then crateDest is not {@code null}
     * @param crateDest optional crate destination. If not {@code null} then crateDest is not {@code null}
     * @return the path between the two points
     */
    public static Node findPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        checkInput(playerStart, playerDest, crateStart, crateDest);

        Node n = new Node(null, playerStart, crateStart, null);

        if (n.isEndNode(playerDest, crateDest)) {
            return n;
        }

        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.offer(n);
        visited.add(n);

        Node solution = null;
        while (!queue.isEmpty() && solution == null) {
            Node node = queue.poll();

            TileInfo player = node.player();
            TileInfo crate = node.crate();

            if (crate != null) {
                crate.addCrate();
            }

            for (Direction direction : Direction.VALUES) {
                TileInfo adj = player.adjacent(direction);

                Node child;
                if (adj.isSolid()) {
                    if (crate == null || !crate.isAt(adj)) {
                        continue;
                    }

                    TileInfo adjAdj = adj.adjacent(direction);
                    if (adjAdj.isSolid()) {
                        continue;
                    }

                    child = new Node(node, adj, adjAdj, new Move(direction, true));
                } else {
                    child = new Node(node, adj, crate, new Move(direction, false));
                }

                if (child.isEndNode(playerDest, crateDest)) {
                    solution = child;
                    break;
                }

                if (visited.add(child)) {
                    queue.offer(child);
                }
            }

            if (crate != null) {
                crate.removeCrate();
            }
        }

        if (crateStart != null) {
            crateStart.addCrate();
        }

        return solution;
    }

    private static void checkInput(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        Objects.requireNonNull(playerStart, "No start pos");
        if (playerStart.anyCrate()) {
            throw new IllegalArgumentException("Player on a wall/crate");
        }

        if (playerDest != null) {
            if (playerDest.isWall()) {
                throw new IllegalArgumentException("Player destination is a wall");
            }
            if (playerDest.anyCrate() && !playerDest.isAt(crateStart)) {
                throw new IllegalArgumentException("Player destination is a crate that is not allowed to move");
            }
        }

        if (crateStart != null) {
            if (!crateStart.anyCrate()) {
                throw new IllegalArgumentException("Start crate not a crate");
            }
            Objects.requireNonNull(crateDest, "Crate destination is null");
            if (!crateDest.isAt(crateStart) && crateDest.isSolid()) {
                throw new IllegalArgumentException("Crate destination is a wall/crate");
            }
        } else {
            Objects.requireNonNull(playerDest, "No player destination set whereas no crate will be moved");
        }
    }


    /**
     * Used by {@link #findPath(TileInfo, TileInfo, TileInfo, TileInfo)} to find a path. It represents
     * a node in a graph.
     *
     * @param parent the parent node
     * @param player player position
     * @param crate crate position
     * @param move the move made by the player to move from the parent node to this node
     */
    public record Node(Node parent, TileInfo player, TileInfo crate, Move move) {

        public boolean isEndNode(TileInfo playerDest, TileInfo crateDest) {
            return (playerDest == null || playerDest.isAt(player))
                    && (crateDest == null || crateDest.isAt(crate));
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;

            if (!Objects.equals(player, node.player)) return false;
            return Objects.equals(crate, node.crate);
        }

        @Override
        public int hashCode() {
            int result = player != null ? player.hashCode() : 0;
            result = 31 * result + (crate != null ? crate.hashCode() : 0);
            return result;
        }
    }
}
