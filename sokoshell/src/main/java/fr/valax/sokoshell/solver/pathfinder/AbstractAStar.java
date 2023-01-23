package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.Move;
import fr.valax.sokoshell.solver.board.tiles.MutableTileInfo;

import java.util.PriorityQueue;

/**
 * Abstract implementation of A*.
 */
public abstract class AbstractAStar {

    protected MutableTileInfo playerStart;
    protected MutableTileInfo crateStart;
    protected MutableTileInfo playerDest;
    protected MutableTileInfo crateDest;

    protected final PriorityQueue<Node> queue;

    public AbstractAStar(PriorityQueue<Node> queue) {
        this.queue = queue;
    }

    /**
     * @return true if path exists
     * @see #findPath(MutableTileInfo, MutableTileInfo, MutableTileInfo, MutableTileInfo)
     */
    public boolean hasPath(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
        return findPath(playerStart, playerDest, crateStart, crateDest) != null;
    }

    /**
     * It also computes the move field in {@link Node}
     *
     * @see #findPath(MutableTileInfo, MutableTileInfo, MutableTileInfo, MutableTileInfo)
     */
    public Node findPathAndComputeMoves(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
        Node end = findPath(playerStart, playerDest, crateStart, crateDest);

        if (end == null) {
            return null;
        }

        Node current = end;
        while (current.getParent() != null) {
            Node last = current.getParent();

            MutableTileInfo lastPlayer = last.getPlayer();
            MutableTileInfo currPlayer = current.getPlayer();
            Direction dir = Direction.of(currPlayer.getX() - lastPlayer.getX(), currPlayer.getY() - lastPlayer.getY());

            boolean moved = crateStart != null && !current.getCrate().isAt(last.getCrate());
            current.setMove(Move.of(dir, moved));

            current = last;
        }

        return end;
    }

    /**
     * Find a path between (playerStart, crateStart) and (playerDest, crateDest).
     * The returned node may be cached by the implementation. Therefore, if you
     * want to keep the path in memory, you need to copy the path.
     *
     * @param playerStart player start
     * @param playerDest player dest
     * @param crateStart crate start
     * @param crateDest crate dest
     * @return the shortest path as a linked list in reverse.
     */
    public Node findPath(MutableTileInfo playerStart, MutableTileInfo playerDest, MutableTileInfo crateStart, MutableTileInfo crateDest) {
        this.playerStart = playerStart;
        this.crateStart = crateStart;
        this.playerDest = playerDest;
        this.crateDest = crateDest;

        init();
        Node n = initialNode();
        queue.offer(n);

        // int c = 0;
        Node end = null;
        while (!queue.isEmpty()) {
            Node node = queue.poll();

            if (isEndNode(node)) {
                end = node;
                break;
            }

            if (isVisited(node)) {
                continue;
            }

            for (Direction direction : Direction.VALUES) {
                Node child = processMove(node, direction);

                if (child != null) {
                    queue.offer(child);
                }
            }

            markVisited(node);
            // c++;
        }
        // System.out.println(c);

        clean();
        return end;
    }

    /**
     * Decrease the priority of the node in the queue if and only if it is in the queue
     * @param node node
     */
    public void decreasePriority(Node node) {
        // TODO: we do not have a fixed size binary heap that
        //  can efficiently decrease priority (at least O(log n))
        if (queue.remove(node)) { // takes O(n)
            queue.offer(node); // takes O(log n)
        }
    }

    /**
     * Init A*. Usually clear the queue. Called before the search
     */
    protected abstract void init();

    /**
     * Clean the object. Called at the end of the search
     */
    protected abstract void clean();

    /**
     * Returns the initial node.
     * @return the initial node
     */
    protected abstract Node initialNode();

    /**
     *
     * @param parent parent node
     * @param dir direction taken player
     * @return {@code null} if the player cannot move in the specified direction
     * or if the node was already visited. Otherwise, returns child node
     */
    protected abstract Node processMove(Node parent, Direction dir);

    /**
     * Mark the node as visited
     * @param node node
     */
    protected abstract void markVisited(Node node);

    /**
     * @param node node
     * @return {@code true} if the node is visited
     */
    protected abstract boolean isVisited(Node node);

    /**
     * @param node node
     * @return {@code true} if this node represents the solution
     */
    protected abstract boolean isEndNode(Node node);
}
