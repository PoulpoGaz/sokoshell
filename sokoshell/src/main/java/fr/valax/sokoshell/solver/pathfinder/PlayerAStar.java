package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;
import fr.valax.sokoshell.solver.mark.AbstractMarkSystem;
import fr.valax.sokoshell.solver.mark.Mark;
import fr.valax.sokoshell.solver.mark.MarkSystem;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * An 'A*' that can find a path between a start position and an end position for a player.
 * It uses a local mark system.
 */
public class PlayerAStar extends AbstractAStar {

    private final int mapWidth;
    private final MarkSystem system;
    private final Mark[] marks;
    private final Node[] nodes;

    public PlayerAStar(Map map) {
        super(new PriorityQueue<>(map.getWidth() * map.getHeight()));
        this.mapWidth = map.getWidth();
        system = createMarkSystem();
        marks = new Mark[map.getHeight() * map.getWidth()];
        nodes = new Node[map.getHeight() * map.getWidth()];

        for (int i = 0; i < marks.length; i++) {
            marks[i] = system.newMark();
            nodes[i] = new Node();
        }
    }

    private MarkSystem createMarkSystem() {
        return new AbstractMarkSystem() {
            @Override
            public void reset() {
                for (Mark m : PlayerAStar.this.marks) {
                    m.unmark();
                }
            }
        };
    }

    private int toIndex(TileInfo player) {
        return player.getY() * mapWidth + player.getX();
    }

    @Override
    protected void init() {
        system.unmarkAll();
        queue.clear();
    }

    @Override
    protected void clean() {

    }

    @Override
    protected Node initialNode() {
        int i = toIndex(playerStart);

        marks[i].mark();
        Node init = nodes[i];
        init.setInitial(playerStart, null, heuristic(playerDest));
        return init;
    }

    @Override
    protected Node processMove(Node parent, Direction dir) {
        TileInfo player = parent.getPlayer();
        TileInfo dest = player.adjacent(dir);

        if (!dest.isSolid()) {
            int i = toIndex(dest);

            Mark mark = marks[i];
            if (mark.isMarked()) {
                return null;
            }
            mark.mark();

            Node node = nodes[i];
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
