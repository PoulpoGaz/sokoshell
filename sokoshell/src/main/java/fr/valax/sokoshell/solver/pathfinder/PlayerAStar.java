package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import fr.valax.sokoshell.solver.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.mark.Mark;
import fr.valax.sokoshell.solver.mark.MarkSystem;

import java.util.PriorityQueue;

/**
 * An 'A*' that can find a path between a start position and an end position for a player.
 * It uses a local mark system.
 */
public class PlayerAStar extends AbstractAStar {

    private final MarkSystem system;
    private final Mark[][] mark;
    private final Node[][] nodes;

    public PlayerAStar(Map map) {
        super(new PriorityQueue<>(map.getWidth() * map.getHeight()));
        system = createMarkSystem();
        mark = new Mark[map.getHeight()][map.getWidth()];
        nodes = new Node[map.getHeight()][map.getWidth()];

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                mark[y][x] = system.newMark();
                nodes[y][x] = new Node();
            }
        }
    }

    private MarkSystem createMarkSystem() {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                for (Mark[] marks : PlayerAStar.this.mark) {
                    for (Mark m : marks) {
                        m.unmark();
                    }
                }
            }
        };
    }

    @Override
    protected void clear() {
        system.unmarkAll();
        queue.clear();
    }

    @Override
    protected void clean() {

    }

    @Override
    protected Node initialNode() {
        Node init = nodes[playerStart.getY()][playerStart.getX()];
        init.setInitial(playerStart, null, heuristic(playerDest));
        return init;
    }

    @Override
    protected void addNode(Node node) {
        TileInfo p = node.getPlayer();

        if (!mark[p.getY()][p.getX()].isMarked()) {
            mark[p.getY()][p.getX()].mark();
            queue.offer(node);
        }
    }

    @Override
    protected Node processMove(Node parent, Direction dir) {
        TileInfo player = parent.getPlayer();
        TileInfo dest = player.adjacent(dir);

        if (!dest.isSolid()) {
            Node node = nodes[dest.getY()][dest.getX()];
            node.set(parent, dest, null, heuristic(dest));
            return node;
        }

        return null;
    }

    protected int heuristic(TileInfo newPlayer) {
        return newPlayer.manhattanDistance(playerDest);
    }

    @Override
    protected boolean isEndNode(Node node) {
        return node.getPlayer().isAt(playerDest);
    }
}
