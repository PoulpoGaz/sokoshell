package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;

import java.util.Objects;

public abstract class MapStyle {

    private static int unnamedIndex = 0;

    protected final String name;
    protected final String author;
    protected final String version;

    public MapStyle(String name, String author, String version) {
        if (name == null) {
            this.name = "Unnamed n°" + (++unnamedIndex);
        } else {
            this.name = name;
        }

        this.author = Objects.requireNonNullElse(author, "none");
        this.version = Objects.requireNonNullElse(version, "0");

    }

    /**
     * Draw the tile at ({@code drawX}; {@code drawY}) with the specified {@code size}.
     * If {@code playerDir} isn't {@code null}, it will also draw the player wit the specified
     * direction. If the size isn't supported by the style, it will try to draw the tile, but
     * it may produce weird results
     *
     * @param g graphics to draw with
     * @param tile the tile to draw
     * @param playerDir not null to draw the player of this direction
     * @param drawX draw x
     * @param drawY draw y
     * @param size size of the tile
     */
    public abstract void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size);

    /**
     * Finds the best size with respect to {@code size} ie finds the
     * nearest and smallest size to size that is correctly supported by
     * the style
     *
     * @param size size
     * @return the best size
     */
    public abstract int findBestSize(int size);

    /**
     * Returns {@code true} if the size is supported by the style.
     * An unsupported size can still be passed to {@link #draw(Graphics, Map, int, int, int, int, int)}
     * but it may produce weird results.
     *
     * @param size size
     * @return {@code true} if the size is supported by the style
     */
    public abstract boolean isSupported(int size);

    public final String getName() {
        return name;
    }

    public final String getAuthor() {
        return author;
    }

    public final String getVersion() {
        return version;
    }
}
