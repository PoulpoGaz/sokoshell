package fr.valax.sokoshell.solver.pathfinder;

import fr.valax.sokoshell.solver.Move;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.Objects;

public class Node implements Comparable<Node> {

    private Node parent;
    private int dist;
    private int heuristic;
    private TileInfo player;
    private TileInfo crate;
    private Move move;

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
    }

    public void set(Node parent, TileInfo player, TileInfo crate, int heuristic) {
        this.parent = parent;
        this.dist = parent.dist + 1;
        this.heuristic = heuristic;
        this.player = player;
        this.crate = crate;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public int getDist() {
        return dist;
    }

    public void setDist(int dist) {
        this.dist = dist;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(int heuristic) {
        this.heuristic = heuristic;
    }

    public TileInfo getPlayer() {
        return player;
    }

    public void setPlayer(TileInfo player) {
        this.player = player;
    }

    public TileInfo getCrate() {
        return crate;
    }

    public void setCrate(TileInfo crate) {
        this.crate = crate;
    }

    public Move getMove() {
        return move;
    }

    public void setMove(Move move) {
        this.move = move;
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
        return Integer.compare(heuristic, o.heuristic);
    }
}