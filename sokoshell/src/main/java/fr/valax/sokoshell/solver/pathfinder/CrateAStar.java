package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.PriorityQueue;

/**
 * Moves a crate from a start position to a destination.
 */
public class CrateAStar extends AbstractAStar {

    private final int mapWidth;
    private final int area;

    private final AStarMarkSystem markSystem;
    private final Node[] nodes;

    public CrateAStar(Board board) {
        super(new PriorityQueue<>(2 * board.getWidth() * board.getHeight()));
        this.mapWidth = board.getWidth();

        area = board.getWidth() * board.getHeight();
        markSystem = new AStarMarkSystem(area * area);

        nodes = new Node[area * area];

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new Node();
        }
    }

    private int toIndex(TileInfo player, TileInfo crate) {
        return (player.getY() * mapWidth + player.getX()) * area + crate.getY() * mapWidth + crate.getX();
    }

    @Override
    protected void init() {
        markSystem.unmarkAll();
        queue.clear();
        crateStart.removeCrate();
    }

    @Override
    protected void clean() {
        crateStart.addCrate();
    }

    @Override
    protected Node initialNode() {
        int i = toIndex(playerStart, crateStart);

        Node init = nodes[i];
        init.setInitial(playerStart, crateStart, heuristic(playerStart, crateStart));
        return init;
    }

    @Override
    protected Node processMove(Node parent, Direction dir) {
        TileInfo player = parent.getPlayer();
        TileInfo crate = parent.getCrate();
        TileInfo playerDest = player.adjacent(dir);
        TileInfo crateDest = crate;

        if (playerDest.isAt(crate)) {
            crateDest = playerDest.adjacent(dir);

            if (crateDest.isSolid()) {
                return null;
            }

            // check deadlock
            if (!crateDest.isAt(this.crateDest) && // not a deadlock is if is destination
                    crateDest.adjacent(dir).isSolid() && // front must be solid
                    (crateDest.adjacent(dir.left()).isSolid() || // perp must be solid
                            crateDest.adjacent(dir.right()).isSolid())) {
                return null;
            }
        } else if (playerDest.isSolid()) {
            return null;
        }

        int i = toIndex(playerDest, crateDest);
        Node node = nodes[i];

        if (markSystem.isMarked(i) || markSystem.isVisited(i)) {
            if (parent.getDist() + 1 + node.getHeuristic() < node.getExpectedDist()) {
                node.changeParent(parent);
                decreasePriority(node);
            }

            return null;
        } else {
            markSystem.mark(i);
            node.set(parent, playerDest, crateDest, heuristic(playerDest, crateDest));

            return node;
        }
    }

    @Override
    protected void markVisited(Node node) {
        markSystem.setVisited(toIndex(node.getPlayer(), node.getCrate()));
    }

    @Override
    protected boolean isVisited(Node node) {
        return markSystem.isVisited(toIndex(node.getPlayer(), node.getCrate()));
    }

    @Override
    protected boolean isEndNode(Node node) {
        return node.getCrate().isAt(crateDest);
    }

    protected int heuristic(TileInfo newPlayer, TileInfo newCrate) {
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
        if (newPlayer.manhattanDistance(newCrate) > 1) {
            h += newPlayer.manhattanDistance(newCrate);
        }

        return h;
    }
}
