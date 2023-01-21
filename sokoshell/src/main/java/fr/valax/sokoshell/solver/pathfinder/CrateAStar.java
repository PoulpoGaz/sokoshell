package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.mark.Mark;
import fr.valax.sokoshell.solver.mark.MarkSystem;

import java.util.PriorityQueue;

/**
 * Moves a crate from a start position to a destination.
 */
public class CrateAStar extends AbstractAStar {

    private final int mapWidth;

    private final MarkSystem system;
    // player, crate
    private final Mark[][] mark;
    private final Node[][] nodes;

    public CrateAStar(Map map) {
        super(new PriorityQueue<>(2 * map.getWidth() * map.getHeight()));
        this.mapWidth = map.getWidth();

        int area = map.getWidth() * map.getHeight();
        system = createMarkSystem();
        mark = new Mark[area][area];
        nodes = new Node[area][area];

        for (int i = 0; i < area; i++) {
            for (int j = 0; j < area; j++) {
                mark[i][j] = system.newMark();
                nodes[i][j] = new Node();
            }
        }
    }

    private MarkSystem createMarkSystem() {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                for (Mark[] marks : CrateAStar.this.mark) {
                    for (Mark m : marks) {
                        m.unmark();
                    }
                }
            }
        };
    }

    private int toIndex(TileInfo tile) {
        return tile.getY() * mapWidth + tile.getX();
    }

    @Override
    protected void init() {
        system.unmarkAll();
        queue.clear();
        crateStart.removeCrate();
    }

    @Override
    protected void clean() {
        crateStart.addCrate();
    }

    @Override
    protected Node initialNode() {
        int i = toIndex(playerStart);
        int j = toIndex(crateStart);

        mark[i][j].mark();
        Node init = nodes[i][j];
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

        } else if (playerDest.isSolid()) {
            return null;
        }

        int i = toIndex(playerDest);
        int j = toIndex(crateDest);
        Mark m = mark[i][j];
        Node node = nodes[i][j];

        if (m.isMarked()) {
            if (parent.getDist() + 1 + node.getHeuristic() < node.getExpectedDist()) {
                crateDest.addCrate();
                BasicStyle.XSB_STYLE.print(crate.getMap(), playerDest.getX(), playerDest.getY());
                crateDest.removeCrate();
                System.out.println("Updating distance from " + node.getDist() + " to " + (parent.getDist() + 1));
                node.changeParent(parent);
                decreasePriority(node);
            }

            return null;
        }
        m.mark();
        node.set(parent, playerDest, crateDest, heuristic(playerDest, crateDest));
        crateDest.addCrate();
        BasicStyle.XSB_STYLE.print(crate.getMap(), playerDest.getX(), playerDest.getY());
        crateDest.removeCrate();
        System.out.println("Dist = " + node.getDist() + "; heuristic = " + node.getHeuristic());

        return node;
    }

    @Override
    protected void markVisited(Node node) {

    }

    @Override
    protected boolean isVisited(Node node) {
        return false;
    }

    @Override
    protected boolean isEndNode(Node node) {
        return node.getCrate().isAt(crateDest);
    }

    protected int heuristic(TileInfo newPlayer, TileInfo newCrate) {
        //int h = newCrate.manhattanDistance(crateDest);

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
        /*if (newPlayer.manhattanDistance(newCrate) > 1) {
            h += newPlayer.manhattanDistance(newCrate);
        }*/

        return 0;
    }
}
