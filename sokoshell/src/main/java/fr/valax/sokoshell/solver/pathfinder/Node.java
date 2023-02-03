package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.board.Move;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.Objects;

/**
 * A node in A*
 */
public class Node implements Comparable<Node> {

    private Node parent;
    private int dist;
    private int heuristic;
    private TileInfo player;
    private TileInfo crate;
    private Move move;

    private int expectedDist;

    public Node() {
    }

    public Node(Node parent,
                int dist, int heuristic,
                TileInfo player, TileInfo crate, Move move) {
        this.parent = parent;
        this.dist = dist;
        this.heuristic = heuristic;
        this.player = player;
        this.crate = crate;
        this.move = move;
    }

    public void setInitial(TileInfo player, TileInfo crate, int heuristic) {
        parent = null;
        dist = 0;
        this.heuristic = heuristic;
        this.player = player;
        this.crate = crate;

        expectedDist = heuristic;
    }

    public void set(Node parent, TileInfo player, TileInfo crate, int heuristic) {
        this.parent = parent;
        this.dist = parent.dist + 1;
        this.heuristic = heuristic;
        this.player = player;
        this.crate = crate;

        expectedDist = dist + heuristic;
    }

    public void changeParent(Node newParent) {
        this.parent = newParent;
        this.dist = newParent.dist + 1;

        expectedDist = dist + heuristic;
    }

    public Node getParent() {
        return parent;
    }

    public int getDist() {
        return dist;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public TileInfo getPlayer() {
        return player;
    }

    public TileInfo getCrate() {
        return crate;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public int getExpectedDist() {
        return expectedDist;
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
        result = 31 * result + (crate != null ? crate.positionHashCode() : 0); // TODO
        return result;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(expectedDist, o.expectedDist);
    }
}