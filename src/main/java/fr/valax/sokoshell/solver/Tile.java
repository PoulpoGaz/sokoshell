package fr.valax.sokoshell.solver;

/**
 * @author darth-mole
 */
public enum Tile {

    FLOOR(false, false),
    WALL(true, false),
    CRATE(true, true),
    CRATE_ON_TARGET(true, true),
    TARGET(false, false);

    private final boolean solid;
    private final boolean crate;

    Tile(boolean solid, boolean crate) {
        this.solid = solid;
        this.crate = crate;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isCrate() {
        return crate;
    }
}
