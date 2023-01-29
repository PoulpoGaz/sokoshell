package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.PriorityQueue;

/**
 * An 'A*' that can find a path between a start position and an end position for a player.
 * It uses a local mark system.
 */
public class PlayerAStar extends AbstractAStar {

    private final int mapWidth;
    private final AStarMarkSystem markSystem;
    private final Node[] nodes;

    public PlayerAStar(Board board) {
        super(new PriorityQueue<>(board.getWidth() * board.getHeight()));
        this.mapWidth = board.getWidth();
        markSystem = new AStarMarkSystem(board.getWidth() * board.getHeight());
        nodes = new Node[board.getHeight() * board.getWidth()];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node();
        }
    }

    private int toIndex(TileInfo player) {
        return player.getY() * mapWidth + player.getX();
    }

    @Override
    protected void init() {
        markSystem.unmarkAll();
        queue.clear();
    }

    @Override
    protected void clean() {

    }

    @Override
    protected Node initialNode() {
        int i = toIndex(playerStart);

        Node init = nodes[i];
        init.setInitial(playerStart, null, heuristic(playerStart));
        return init;
    }

    @Override
    protected Node processMove(Node parent, Direction dir) {
        TileInfo player = parent.getPlayer();
        TileInfo dest = player.adjacent(dir);

        if (dest.isSolid()) {
            return null;
        }

        int i = toIndex(dest);
        Node node = nodes[i];

        if (markSystem.isMarked(i) || markSystem.isVisited(i)) { // the node was added to the queue, therefore node.getExpectedDist() is valid
            if (parent.getDist() + 1 + node.getHeuristic() < node.getExpectedDist()) {
                node.changeParent(parent);
                decreasePriority(node);
            }

            return null;
        } else {
            markSystem.mark(i);
            node.set(parent, dest, null, heuristic(dest));
            return node;
        }
    }

    @Override
    protected void markVisited(Node node) {
        markSystem.setVisited(toIndex(node.getPlayer()));
    }

    @Override
    protected boolean isVisited(Node node) {
        return markSystem.isVisited(toIndex(node.getPlayer()));
    }

    protected int heuristic(TileInfo newPlayer) {
        return newPlayer.manhattanDistance(playerDest);
    }

    @Override
    protected boolean isEndNode(Node node) {
        return node.getPlayer().isAt(playerDest);
    }
}
