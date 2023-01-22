package fr.valax.sokoshell.graphics.style;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.GraphicsUtils;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;
import fr.valax.sokoshell.solver.TileInfo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static org.jline.utils.AttributedStyle.*;

/**
 * A style that map each tile, player and 'player on target' to a {@link StyledCharacter}
 */
public class BasicStyle extends MapStyle {

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
    public void draw(Graphics g, TileInfo tile, Direction playerDir, int drawX, int drawY, int size) {
        StyledCharacter s = translateToStyledCharacter(tile, playerDir);

        g.setChar(s.getChar());
        g.setStyle(s.getStyle());
        g.fillRectangle(drawX, drawY, size, size);
    }

    @Override
    public void draw(Graphics2D g2d, TileInfo tile, Direction playerDir, int drawX, int drawY, int size, int charWidth, int charHeight) {
        StyledCharacter s = translateToStyledCharacter(tile, playerDir);

        g2d.translate(drawX, drawY);
        g2d.scale(size, size);
        GraphicsUtils.draw(g2d, s, 0, 0, charWidth, charHeight, Color.BLACK, Color.WHITE);

        double inv = 1d / size;
        g2d.scale(inv, inv);
        g2d.translate(-drawX, -drawY);
    }

    private StyledCharacter translateToStyledCharacter(TileInfo tile, Direction playerDir) {
        if (playerDir != null) {
            if (tile.isTarget()) {
                return playerOnTarget;
            } else {
                return player;
            }
        } else {
            return switch (tile.getTile()) {
                case FLOOR ->  floor;
                case WALL -> wall;
                case CRATE -> crate;
                case CRATE_ON_TARGET -> crateOnTarget;
                case TARGET -> target;
            };
        }
    }

    @Override
    public BufferedImage createImage(Map map, int playerX, int playerY, Direction playerDir) {
        BufferedImage img = new BufferedImage(map.getWidth() * GraphicsUtils.CHAR_WIDTH,
                map.getHeight() * GraphicsUtils.CHAR_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = img.createGraphics();
        try {
            g2d.setFont(GraphicsUtils.DEFAULT_FONT);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    boolean player = playerX == x && playerY == y;

                    TileInfo tile = map.getAt(x, y);
                    int drawX = x * GraphicsUtils.CHAR_WIDTH;
                    int drawY = y * GraphicsUtils.CHAR_HEIGHT;
                    if (player) {
                        draw(g2d, tile, playerDir, drawX, drawY, 1, GraphicsUtils.CHAR_WIDTH, GraphicsUtils.CHAR_HEIGHT);
                    } else {
                        draw(g2d, tile, null, drawX, drawY, 1, GraphicsUtils.CHAR_WIDTH, GraphicsUtils.CHAR_HEIGHT);
                    }
                }
            }
        } finally {
            g2d.dispose();
        }

        return img;
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
