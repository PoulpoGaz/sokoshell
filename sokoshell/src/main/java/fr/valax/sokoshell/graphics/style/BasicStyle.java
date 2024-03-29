package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static org.jline.utils.AttributedStyle.*;

/**
 * A style that board each tile, player and 'player on target' to a {@link StyledCharacter}
 */
public class BasicStyle extends BoardStyle {

    public static final BasicStyle DEFAULT_STYLE = new BasicStyle(
            "default", SokoShell.NAME, SokoShell.VERSION,
            new StyledCharacter(' ', DEFAULT.background(GREEN)),
            new StyledCharacter(' ', DEFAULT.background(WHITE)),
            new StyledCharacter(' ', DEFAULT.background(YELLOW)),
            new StyledCharacter(' ', DEFAULT.background(CYAN)),
            new StyledCharacter(' ', DEFAULT.background(RED)),
            new StyledCharacter('o', DEFAULT.background(GREEN)),
            new StyledCharacter('o', DEFAULT.background(RED)));

    public static final BasicStyle XSB_STYLE = new BasicStyle(
            "xsb", SokoShell.NAME, SokoShell.VERSION,
            new StyledCharacter(' ', DEFAULT),
            new StyledCharacter('#', DEFAULT),
            new StyledCharacter('$', DEFAULT),
            new StyledCharacter('*', DEFAULT),
            new StyledCharacter('.', DEFAULT),
            new StyledCharacter('@', DEFAULT),
            new StyledCharacter('+', DEFAULT));


    private final StyledCharacter floor;
    private final StyledCharacter wall;
    private final StyledCharacter crate;
    private final StyledCharacter crateOnTarget;
    private final StyledCharacter target;
    private final StyledCharacter player;
    private final StyledCharacter playerOnTarget;

    public BasicStyle(String name, String author, String version,
                      StyledCharacter floor,
                      StyledCharacter wall,
                      StyledCharacter crate,
                      StyledCharacter crateOnTarget,
                      StyledCharacter target,
                      StyledCharacter player,
                      StyledCharacter playerOnTarget) {
        super(name, author, version);
        this.floor = Objects.requireNonNull(floor);
        this.wall = Objects.requireNonNull(wall);
        this.crate = Objects.requireNonNull(crate);
        this.crateOnTarget = Objects.requireNonNull(crateOnTarget);
        this.target = Objects.requireNonNull(target);
        this.player = Objects.requireNonNull(player);
        this.playerOnTarget = Objects.requireNonNull(playerOnTarget);
    }

    @Override
    public void draw(Graphics g,
                     TileInfo tile, boolean player, Direction playerDir,
                     int drawX, int drawY, int size) {
        draw(g, tile.getTile(), player, playerDir, drawX, drawY, size);
    }

    @Override
    public void draw(Graphics g, Tile tile, boolean player, Direction playerDir, int drawX, int drawY, int size) {
    StyledCharacter s = translateToStyledCharacter(tile, player);

        g.setChar(s.getChar());
        g.setStyle(s.getStyle());
        g.fillRectangle(drawX, drawY, size, size);
    }

    @Override
    public void draw(Graphics2D g2d,
                     TileInfo tile, boolean player, Direction playerDir,
                     int drawX, int drawY, int size, int charWidth, int charHeight) {
        draw(g2d, tile.getTile(), player, playerDir, drawX, drawY, size, charWidth, charHeight);
    }

    @Override
    public void draw(Graphics2D g2d, Tile tile, boolean player, Direction playerDir, int drawX, int drawY, int size, int charWidth, int charHeight) {
        StyledCharacter s = translateToStyledCharacter(tile, player);

        g2d.translate(drawX, drawY);
        g2d.scale(size, size);
        GraphicsUtils.draw(g2d, s, 0, 0, charWidth, charHeight, Color.BLACK, Color.WHITE);

        double inv = 1d / size;
        g2d.scale(inv, inv);
        g2d.translate(-drawX, -drawY);
    }

    private StyledCharacter translateToStyledCharacter(Tile tile, boolean player) {
        if (player) {
            if (tile == Tile.TARGET) {
                return playerOnTarget;
            } else {
                return this.player;
            }
        } else {
            return switch (tile) {
                case FLOOR ->  floor;
                case WALL -> wall;
                case CRATE -> crate;
                case CRATE_ON_TARGET -> crateOnTarget;
                case TARGET -> target;
            };
        }
    }

    @Override
    public BufferedImage createImage(Board board, int playerX, int playerY, Direction playerDir) {
        return new CreateImageHelper().initAndCreateImage(false, this, 1, board, playerX, playerY, playerDir);
    }

    @Override
    public BufferedImage createImageWithLegend(Board board, int playerX, int playerY, Direction playerDir) {
        return new CreateImageHelper().initAndCreateImage(true, this, 1, board, playerX, playerY, playerDir);
    }

    @Override
    public int findBestSize(int size) {
        return 1;
    }

    @Override
    public boolean isSupported(int size) {
        return size > 0;
    }
}
