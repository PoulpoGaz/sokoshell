package fr.valax.sokoshell.solver;

public class Tunnel {

    private final TileInfo start;
    private final TileInfo end;

    private boolean oneway;

    public Tunnel(TileInfo start, TileInfo end) {
        this.start = start;
        this.end = end;
    }

    public TileInfo getStart() {
        return start;
    }

    public TileInfo getEnd() {
        return end;
    }
}
