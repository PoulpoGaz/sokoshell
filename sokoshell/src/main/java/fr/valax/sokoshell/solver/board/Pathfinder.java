package fr.valax.sokoshell.solver.board;

import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;

import java.util.*;

/**
 * This class uses A* algorithm to find a path in a map.
 */
public class Pathfinder {

    private final AStar cratePlayerAStar = new AStarFull();
    private final AStar crateAStar = new AStarCrateOnly();
    private final AStar playerAStar = new AStarPlayerOnly();

    private final PriorityQueue<Node> queue;
    private final Set<Node> visited;

    public Pathfinder() {
        queue = new PriorityQueue<>();
        visited = new HashSet<>();
    }

    public boolean hasPath(MutableTileInfo player, MutableTileInfo playerDest, MutableTileInfo crate, MutableTileInfo crateDest) {
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
    public Node findPath(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
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

    /**
     * This A* is able to find a path that allow the player to move from an initial position to
     * a destination and move a crate from another initial position to a destination.
     *
     * @return an A* that can find a path between a start position for the player, a start position for a crate
     * and a destination for the player and the crate.
     */
    public AStar getCratePlayerAStar() {
        return cratePlayerAStar;
    }

    /**
     * @return an A* that can find a path between a start position and a destination for the player
     */
    public AStar getPlayerAStar() {
        return playerAStar;
    }

    /**
     * Let p, the position of the player.
     * Let c, the position of a crate.
     * let d, the destination o the crate.
     * This A* is able to find a path that allow the player to move one crate to a destination.
     *
     * @return an A* that can find a path between a start position, a crate position and a destination for the crate
     */
    public AStar getCrateAStar() {
        return crateAStar;
    }


    protected class AStarPlayerOnly extends AStar {

        @Override
        protected Node initialNode() {
            return new Node(null, 0, 0, playerStart, null, null);
        }

        @Override
        protected Node processMove(Node parent, Direction dir) {
            MutableTileInfo player = parent.player();
            MutableTileInfo adj = player.adjacent(dir);

            if (!adj.isSolid()) {
                return child(parent, adj, null, Move.of(dir, false));
            }

            return null;
        }

        @Override
        protected int heuristic(MutableTileInfo newPlayer, MutableTileInfo newCrate) {
            return newPlayer.manhattanDistance(playerDest);
        }

        @Override
        protected boolean isEndNode(Node node) {
            return node.player.isAt(playerDest);
        }
    }


    protected class AStarCrateOnly extends AStar {

        @Override
        protected Node initialNode() {
            return new Node(null, 0, 0, playerStart, crateStart, null);
        }

        @Override
        protected void preInit() {
            super.preInit();
            crateStart.removeCrate();
        }

        @Override
        protected void postInit() {
            super.postInit();
            crateStart.addCrate();
        }

        @Override
        protected Node processMove(Node parent, Direction dir) {
            MutableTileInfo player = parent.player();
            MutableTileInfo crate = parent.crate();
            MutableTileInfo adj = player.adjacent(dir);

            if (adj.isAt(crate)) {
                MutableTileInfo adjAdj = adj.adjacent(dir);

                if (adjAdj.isSolid()) {
                    return null;
                }

                return child(parent, adj, adjAdj, Move.of(dir, true));
            } else if (!adj.isSolid()) {
                return child(parent, adj, crate, Move.of(dir, false));
            }

            return null;
        }

        @Override
        protected int heuristic(MutableTileInfo newPlayer, MutableTileInfo newCrate) {
            int h = newCrate.manhattanDistance(crateDest);

            /* the player first need to move near the crate to push it
               may not be optimal for level like this:

                #########
                #       #
                # ##### #
                # ##### #
                # ##### #
                 @$     # The player needs to do a detour to push the crate
                # #######
             */
            if (newPlayer.manhattanDistance(newCrate) > 2) {
                h += newPlayer.manhattanDistance(newCrate);
            }

            return h;
        }

        @Override
        protected boolean isEndNode(Node node) {
            return node.crate.isAt(crateDest);
        }
    }


    protected class AStarFull extends AStarCrateOnly {

        @Override
        protected int heuristic(MutableTileInfo newPlayer, MutableTileInfo newCrate) {
            /*
                Try to first move the player near the crate
                Then push the crate to his destination
                Finally moves the player to his destination
             */
            int remaining = newCrate.manhattanDistance(crateDest);
            if (remaining == 0) {
                remaining = newPlayer.manhattanDistance(playerDest);
            } else {
                if (newPlayer.manhattanDistance(newCrate) > 2) {
                    remaining += newPlayer.manhattanDistance(newCrate);
                }
            }

            return remaining;
        }

        @Override
        protected boolean isEndNode(Node node) {
            return node.player.isAt(playerDest) && node.crate.isAt(crateDest);
        }
    }


    public abstract class AStar {

        protected MutableTileInfo playerStart;
        protected MutableTileInfo crateStart;
        protected MutableTileInfo playerDest;
        protected MutableTileInfo crateDest;

        public boolean hasPath(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
            return findPath(playerStart, playerDest, crateStart, crateDest) != null;
        }

        public Node findPath(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
            this.playerStart = playerStart;
            this.crateStart = crateStart;
            this.playerDest = playerDest;
            this.crateDest = crateDest;

            Node n = initialNode();

            if (isEndNode(n)) {
                return n;
            }

            preInit();

            queue.offer(n);
            visited.add(n);

            while (!queue.isEmpty()) {
                Node node = queue.poll();

                for (Direction direction : Direction.VALUES) {
                    Node child = processMove(node, direction);

                    if (child != null) {
                        if (isEndNode(child)) {
                            postInit();
                            return child;
                        }

                        if (visited.add(child)) {
                            queue.offer(child);
                        }
                    }
                }
            }

            postInit();
            return null;
        }

        protected void preInit() {

        }

        protected void postInit() {
            queue.clear();
            visited.clear();
        }

        protected Node child(Node parent, MutableTileInfo newPlayer, MutableTileInfo newCrate, Move move) {
            return new Node(parent,
                    parent.dist() + 1,
                    parent.dist() + 1 + heuristic(newPlayer, newCrate),
                    newPlayer, newCrate, move);
        }

        protected abstract Node initialNode();

        protected abstract Node processMove(Node parent, Direction dir);

        protected abstract int heuristic(MutableTileInfo newPlayer, MutableTileInfo newCrate);

        protected abstract boolean isEndNode(Node node);
    }

    public record Node(Node parent,
                       int dist, int heuristic,
                       MutableTileInfo player, MutableTileInfo crate, Move move) implements Comparable<Node> {

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
