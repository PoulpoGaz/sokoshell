package fr.valax.sokoshell.solver.board.tiles;

/**
 * Represents the content of a case of the board.
<<<<<<< HEAD
 * @author darth-mole
=======
>>>>>>> refs/remotes/origin/main
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

    /**
     * Tells whether objects (i.e. player or crates) can move through the case or not.
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * Tells whether the case is occupied by a crate (on a target or not) or not.
     */
    public boolean isCrate() {
        return crate;
    }
}
