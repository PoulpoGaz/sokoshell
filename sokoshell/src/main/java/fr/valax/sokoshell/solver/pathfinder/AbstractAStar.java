package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Move;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.PriorityQueue;

public abstract class AbstractAStar {

    protected TileInfo playerStart;
    protected TileInfo crateStart;
    protected TileInfo playerDest;
    protected TileInfo crateDest;

    protected final PriorityQueue<Node> queue;

    public AbstractAStar(PriorityQueue<Node> queue) {
        this.queue = queue;
    }

    public boolean hasPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        return getEndNode(playerStart, playerDest, crateStart, crateDest) != null;
    }

    public Node findPath(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        Node end = getEndNode(playerStart, playerDest, crateStart, crateDest);

        if (end == null) {
            return null;
        }

        Node current = end;
        while (end.getParent() != null) {
            Node last = current.getParent();

            TileInfo lastPlayer = last.getPlayer();
            TileInfo currPlayer = current.getPlayer();
            Direction dir = Direction.of(currPlayer.getX() - lastPlayer.getX(), currPlayer.getY() - lastPlayer.getY());

            boolean moved = crateStart != null && !current.getCrate().isAt(last.getCrate());
            current.setMove(Move.of(dir, moved));

            current = last;
        }

        return end;
    }

    public Node getEndNode(TileInfo playerStart, TileInfo playerDest, TileInfo crateStart, TileInfo crateDest) {
        this.playerStart = playerStart;
        this.crateStart = crateStart;
        this.playerDest = playerDest;
        this.crateDest = crateDest;

        Node n = initialNode();

        if (isEndNode(n)) {
            return n;
        }

        clear();
        addNode(n);

        Node end = null;
        loop:
        while (!queue.isEmpty()) {
            Node node = queue.poll();

            for (Direction direction : Direction.VALUES) {
                Node child = processMove(node, direction);

                if (child != null) {
                    if (isEndNode(child)) {
                        end = child;
                        break loop;
                    }

                    addNode(child);
                }
            }
        }

        clean();
        return end;
    }

    /**
     * Clear the queue. Called before the search
     */
    protected abstract void clear();

    /**
     * Clean the object. Called at the end of the search
     */
    protected abstract void clean();

    protected abstract Node initialNode();

    protected abstract void addNode(Node node);

    protected abstract Node processMove(Node parent, Direction dir);

    protected abstract boolean isEndNode(Node node);
}
