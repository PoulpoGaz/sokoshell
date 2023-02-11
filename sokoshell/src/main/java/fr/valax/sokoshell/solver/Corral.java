package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Corral {

    protected final int id;

    protected int topX;
    protected int topY;

    /**
     * All crates that inside the corral or surrounding the corral
     */
    protected final List<TileInfo> barrier = new ArrayList<>();
    protected final List<TileInfo> crates = new ArrayList<>();
    protected boolean containsPlayer;
    protected boolean isPICorral;

    public Corral(int id) {
        this.id = id;
    }

    public int getTopX() {
        return topX;
    }

    public int getTopY() {
        return topY;
    }

    public List<TileInfo> getBarrier() {
        return barrier;
    }

    public List<TileInfo> getCrates() {
        return crates;
    }

    public boolean containsPlayer() {
        return containsPlayer;
    }

    public boolean isPICorral() {
        return isPICorral;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Corral corral)) return false;

        return id == corral.id;
    }
}
