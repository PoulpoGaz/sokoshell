package fr.valax.sokoshell.solver;

import java.util.*;

/**
 * This class uses A* algorithm to find a path in a map.
 */
public class Pathfinder {

    private final AStar cratePlayerAStar = new AStarFull();
    private final AStar crateAStar = new AStarCrateOnly();
    private final AStar playerAStar = new AStarPlayerOnly();

    private PriorityQueue<Node> queue;
    private Set<Node> visited;

    public Pathfinder() {
        queue = new PriorityQueue<>();
        visited = new HashSet<>();
    }

    public boolean hasPath(TileInfo player, TileInfo playerDest, TileInfo crate, TileInfo crateDest) {
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
    public Node findPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        Objects.requireNonNull(playerStart, "Player start can't be null");

        if (playerStart.isSolid()) {
            throw new IllegalArgumentException("Player start is solid");
        }

        if (playerDest != null) {
            if (crateStart != null || crateDest != null) {
                if (playerDest.isWall()) {
                    throw new IllegalArgumentException("Player destination can't be a wall");
                }

                Objects.requireNonNull(crateStart, "Crate start can't be null when crateDest is not null");
                Objects.requireNonNull(crateDest, "Crate destination can't be null when crateStart is not null");

                if (!crateStart.anyCrate()) {
                    throw new IllegalArgumentException("Crate start isn't a crate");
                }

                if (!crateDest.isAt(crateStart) && crateDest.isSolid()) {
                    throw new IllegalArgumentException("Crate destination is solid");
                }

                return cratePlayerAStar.findPath(playerStart, playerDest, crateStart, crateDest);
            } else {
                if (playerDest.isSolid()) {
                    throw new IllegalArgumentException("Player destination can't be solid");
                }

                return playerAStar.findPath(playerStart, playerDest, null, null);
            }

        } else {
            Objects.requireNonNull(crateStart, "Crate start can't be null when no player destination is set");
            Objects.requireNonNull(crateDest, "Crate destination can't be null when no player destination is set");

            if (!crateStart.anyCrate()) {
                throw new IllegalArgumentException("Crate start isn't a crate");
            }

            if (!crateDest.isAt(crateStart) && crateDest.isSolid()) {
                throw new IllegalArgumentException("Crate destination is solid");
            }

            return crateAStar.findPath(playerStart, null, crateStart, crateDest);
        }
    }

    public AStar getCratePlayerAStar() {
        return cratePlayerAStar;
    }

    public AStar getPlayerAStar() {
        return playerAStar;
    }

    public AStar getCrateAStar() {
        return crateAStar;
    }


    protected class AStarPlayerOnly extends AStar {

        @Override
        protected Node initialNode(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            return Node.playerStartNode(playerStart);
        }

        @Override
        protected Node processMove(Node parent, Direction dir) {
            TileInfo player = parent.player();
            TileInfo adj = player.adjacent(dir);

            if (!adj.isSolid()) {
                return parent.childPlayer(adj, Move.of(dir, false));
            }

            return null;
        }

        @Override
        protected boolean isEndNode(Node node, TileInfo playerDest, TileInfo crateDest) {
            return node.isEndNodePlayer(playerDest);
        }
    }


    protected class AStarCrateOnly extends AStar {

        @Override
        protected void preInit(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            crateStart.removeCrate();
        }

        @Override
        protected void postInit(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            super.postInit(playerStart, playerDest, crateStart, crateDest);
            crateStart.addCrate();
        }

        @Override
        protected Node initialNode(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            return Node.startNode(playerStart, crateStart);
        }

        @Override
        protected Node processMove(Node parent, Direction dir) {
            TileInfo player = parent.player();
            TileInfo crate = parent.crate();
            TileInfo adj = player.adjacent(dir);

            if (adj.isAt(crate)) {
                TileInfo adjAdj = adj.adjacent(dir);

                if (adjAdj.isSolid()) {
                    return null;
                }

                return parent.childCrate(adj, adjAdj, Move.of(dir, true));
            } else if (!adj.isSolid()) {
                return parent.childCrate(adj, crate, Move.of(dir, false));
            }

            return null;
        }

        @Override
        protected boolean isEndNode(Node node, TileInfo playerDest, TileInfo crateDest) {
            return node.isEndNodeCrate(crateDest);
        }
    }


    protected class AStarFull extends AStarCrateOnly {

        @Override
        protected Node processMove(Node parent, Direction dir) {
            TileInfo player = parent.player();
            TileInfo crate = parent.crate();
            TileInfo adj = player.adjacent(dir);

            if (adj.isAt(crate)) {
                TileInfo adjAdj = adj.adjacent(dir);

                if (adjAdj.isSolid()) {
                    return null;
                }

                return parent.child(adj, adjAdj, Move.of(dir, true));
            } else if (!adj.isSolid()) {
                return parent.child(adj, crate, Move.of(dir, false));
            }

            return null;
        }

        @Override
        protected boolean isEndNode(Node node, TileInfo playerDest, TileInfo crateDest) {
            return node.isEndNode(playerDest, crateDest);
        }
    }


    protected abstract class AStar {

        public boolean hasPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            return findPath(playerStart, playerDest, crateStart, crateDest) != null;
        }

        public Node findPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            Node n = initialNode(playerStart, playerDest, crateStart, crateDest);

            if (isEndNode(n, playerDest, crateDest)) {
                return n;
            }

            preInit(playerStart, playerDest, crateStart, crateDest);

            queue.offer(n);
            visited.add(n);

            while (!queue.isEmpty()) {
                Node node = queue.poll();

                for (Direction direction : Direction.VALUES) {
                    Node child = processMove(node, direction);

                    if (child != null) {
                        if (isEndNode(child, playerDest, crateDest)) {
                            postInit(playerStart, playerDest, crateStart, crateDest);
                            return child;
                        }

                        if (visited.add(child)) {
                            queue.offer(child);
                        }
                    }
                }
            }

            postInit(playerStart, playerDest, crateStart, crateDest);
            return null;
        }

        protected void preInit(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {

        }

        protected void postInit(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
            queue.clear();
            visited.clear();
        }

        protected abstract Node initialNode(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest);

        protected abstract Node processMove(Node parent, Direction dir);

        protected abstract boolean isEndNode(Node node, TileInfo playerDest, TileInfo crateDest);
    }

    public record Node(Node parent,
                       int cost, int heuristic,
                       TileInfo player, TileInfo crate, Move move) implements Comparable<Node> {

        public static Node playerStartNode(TileInfo player) {
            return new Node(null, 0, 0, player, null, null);
        }

        public static Node startNode(TileInfo player, TileInfo crate) {
            return new Node(null, 0, 0, player, crate, null);
        }

        public boolean isEndNodePlayer(TileInfo playerDest) {
            return playerDest.isAt(player);
        }

        public Node childPlayer(TileInfo newPlayer, Move move) {
            int h = cost + 1 +
                    newPlayer.manhattanDistance(player);

            return new Node(this, cost + 1, h, newPlayer, null, move);
        }


        public boolean isEndNodeCrate(TileInfo crateDest) {
            return crateDest.isAt(crate);
        }

        public Node childCrate(TileInfo newPlayer, TileInfo newCrate, Move move) {
            int h = cost + 1 +
                    newCrate.manhattanDistance(crate);

            return new Node(this, cost + 1, h, newPlayer, newCrate, move);
        }


        public boolean isEndNode(TileInfo playerDest, TileInfo crateDest) {
            return isEndNodePlayer(playerDest) && isEndNodeCrate(crateDest);
        }

        public Node child(TileInfo newPlayer, TileInfo newCrate, Move move) {
            int h = cost + 1 +
                    newCrate.manhattanDistance(crate) +
                    newPlayer.manhattanDistance(player);

            return new Node(this, cost + 1, h, newPlayer, newCrate, move);
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
            int result = player != null ? player.positionHashCode() : 0;
            result = 31 * result + (crate != null ? crate.positionHashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(heuristic, o.heuristic);
        }
    }
}
