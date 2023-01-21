package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.mark.Mark;
import fr.valax.sokoshell.solver.mark.MarkSystem;

import java.util.PriorityQueue;

/**
 * Moves a crate from a start position to a destination.
 */
public class CrateAStar extends AbstractAStar {

    private final Map map;
    private final int area;

    private final MarkSystem system;
    private final Mark[] mark;
    private final Node[] nodes;

    public CrateAStar(Map map) {
        super(new PriorityQueue<>(2 * map.getWidth() * map.getHeight()));
        this.map = map;
        this.area = map.getWidth() * map.getHeight();
        system = createMarkSystem();
        mark = new Mark[2 * area];
        nodes = new Node[2 * area];

        for (int i = 0; i < mark.length; i++) {
            mark[i] = system.newMark();
            nodes[i] = new Node();
        }
    }

    private MarkSystem createMarkSystem() {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                for (Mark m : CrateAStar.this.mark) {
                    m.unmark();
                }
            }
        };
    }

    private int toIndex(TileInfo player, TileInfo crate) {
        return player.getY() * map.getWidth() + player.getX()
                + area +
                crate.getY() * map.getWidth() + crate.getX();
    }

    private Node getNode(TileInfo player, TileInfo crate) {
        return nodes[toIndex(player, crate)];
    }

    private Mark getMark(TileInfo player, TileInfo crate) {
        return mark[toIndex(player, crate)];
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
        int i = toIndex(playerStart, crateStart);

        mark[i].mark();
        Node init = nodes[i];
        init.setInitial(playerStart, null, heuristic(playerDest, crateDest));
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

        int i = toIndex(playerDest, crateDest);
        Mark m = mark[i];
        if (m.isMarked()) {
            return null;
        }
        m.mark();

        Node n = nodes[i];
        n.set(parent, playerDest, crateDest, heuristic(playerDest, crateDest));

        return n;
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
        if (newPlayer.manhattanDistance(newCrate) > 2) {
            h += newPlayer.manhattanDistance(newCrate);
        }

        return h;
    }
}
