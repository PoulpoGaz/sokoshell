package fr.valax.sokoshell.solver;

import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Corral {

    protected int topX;
    protected int topY;

    /**
     * All crates that inside the corral or surrounding the corral
     */
    protected final Set<TileInfo> crates = new HashSet<>();

    /**
     * Tiles that are inside the corral.
     */
    protected final List<TileInfo> tiles = new ArrayList<>();

    protected boolean containsPlayer;

    public Corral() {

    }

    public boolean containsPlayer() {
        return containsPlayer;
    }
}
