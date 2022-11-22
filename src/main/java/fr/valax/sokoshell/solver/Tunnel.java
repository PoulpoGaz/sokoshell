package fr.valax.sokoshell.solver;

public class Tunnel {

    private TileInfo start;
    private TileInfo end;

    // the tile outside the tunnel adjacent to start
    private TileInfo startOut;

    // the tile outside the tunnel adjacent to end
    private TileInfo endOut;


    public TileInfo getStart() {
        return start;
    }

    public void setStart(TileInfo start) {
        this.start = start;
    }

    public TileInfo getEnd() {
        return end;
    }

    public void setEnd(TileInfo end) {
        this.end = end;
    }

    public TileInfo getStartOut() {
        return startOut;
    }

    public void setStartOut(TileInfo startOut) {
        this.startOut = startOut;
    }

    public TileInfo getEndOut() {
        return endOut;
    }

    public void setEndOut(TileInfo endOut) {
        this.endOut = endOut;
    }
}
