package fr.valax.sokoshell;

import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.tiles.Tile;
import fr.valax.sokoshell.solver.board.tiles.TileInfo;
import fr.valax.sokoshell.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Exporter {

    private Board board;
    private int playerX = -1;
    private int playerY = -1;
    private Direction playerDir;

    private boolean deadTiles;
    private boolean rooms;
    private boolean tunnels;
    private boolean legend;
    private boolean xsb;

    private Path out;

    public Exporter() {

    }

    public Path silentExport() {
        try {
            return export();
        } catch (IOException e) {
            e.printStackTrace();
            Utils.append(e, Path.of("errors"));
            return null;
        }
    }

    public Path export() throws IOException {
        Path out = this.out;
        if (out == null) {
            out = SokoShell.EXPORT_FOLDER.resolve("level");
        }

        out = setExtension(out);
        out = Utils.checkExists(out);

        Path parent = out.getParent();
        if (parent != null && Files.notExists(parent)) {
            Files.createDirectories(parent);
        }

        if (xsb) {
            exportXSB(out);
        } else {
            BufferedImage image = createImage();
            ImageIO.write(image, "png", out.toFile());
        }

        return out;
    }

    private Path setExtension(Path out) {
        String extension;
        if (xsb) {
            extension = ".xsb";
        } else {
            extension = ".png";
        }

        Path parent = out.getParent();

        if (parent == null) {
            return Path.of(out + extension);
        } else {
            return parent.resolve(out.getFileName() + extension);
        }
    }

    private BufferedImage createImage() {
        BoardStyle style = SokoShell.INSTANCE.getBoardStyle();

        boolean oldDeadTiles = style.isDrawDeadTiles();
        boolean oldRooms = style.isDrawRooms();
        boolean oldTunnel = style.isDrawTunnels();

        style.setDrawDeadTiles(deadTiles);
        style.setDrawRooms(rooms);
        style.setDrawTunnels(tunnels);

        BufferedImage image;
        if (legend) {
            image = style.createImageWithLegend(board, playerX, playerY, playerDir);
        } else {
            image = style.createImage(board, playerX, playerY, playerDir);
        }

        style.setDrawDeadTiles(oldDeadTiles);
        style.setDrawRooms(oldRooms);
        style.setDrawTunnels(oldTunnel);

        return image;
    }

    private void exportXSB(Path out) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            boolean hasDeadTiles = false;
            boolean hasRooms = board.getRooms() != null && board.getRooms().size() > 0;
            boolean hasTunnels = board.getTunnels() != null && board.getTunnels().size() > 0;

            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {
                    TileInfo tileInfo = board.getAt(x, y);
                    if (tileInfo.isDeadTile()) {
                        hasDeadTiles = true;
                    }

                    bw.write(getChar(tileInfo));
                }

                bw.newLine();
            }

            bw.newLine();

            // only write LaTeX if necessary
            if (!legend && (!deadTiles || !hasDeadTiles) && (!rooms || !hasRooms) && (!tunnels || !hasTunnels)) {
                return;
            }

            // need to write LaTeX

            bw.write("""
                    LATEX
                    
                    \\begin{document}
                        \\begin{tikzpicture}
                            \\begin{sokoban}[$WIDTH$, $HEIGHT$""");
            if (legend) {
                bw.append(", sok/numbers");
            }
            bw.append("]{$FILE$}");
            bw.newLine();

            for (int y = 0; y < board.getHeight(); y++) {
                for (int x = 0; x < board.getWidth(); x++) {
                    TileInfo tileInfo = board.getAt(x, y);
                    String color = null;
                    if (tileInfo.isDeadTile() && deadTiles) {
                        color = "red";
                    } else if (tileInfo.isInARoom() && rooms) {
                        color = "blue";
                    } else if (tileInfo.isInATunnel() && tunnels) {
                        color = "yellow";
                    }

                    if (color != null) {
                        bw.append("            \\fill[%s, fill opacity=0.2] (%d, %d) rectangle +(1, 1);".formatted(color, x, y));
                        bw.newLine();
                    }
                }
            }

            bw.append("""
                            \\end{sokoban}
                        \\end{tikzpicture}
                    \\end{document}
                    """);
        }
    }

    private char getChar(TileInfo tile) {
        boolean player = playerX == tile.getX() && playerY == tile.getY();

        Tile t = tile.getTile();
        if (player) {
            if (playerDir == null) {
                if (t == Tile.FLOOR) {
                    return '@';
                } else {
                    return '+';
                }
            } else {
                char c = switch (playerDir) {
                    case UP -> 'u';
                    case RIGHT -> 'r';
                    case DOWN -> 'd';
                    case LEFT -> 'l';
                };

                if (t == Tile.FLOOR) {
                    return c;
                }

                return Character.toUpperCase(c);
            }
        } else {
            return switch (t) {
                case WALL -> '#';
                case CRATE -> '$';
                case CRATE_ON_TARGET -> '*';
                case FLOOR -> ' ';
                case TARGET -> '.';
            };
        }
    }

    public void setLevel(Level level) {
        setLevel(level, level, level.getPlayerX(), level.getPlayerY(), null);
    }

    public void setLevel(Level level, Board board, int playerX, int playerY, Direction playerDir) {
        out = SokoShell.INSTANCE.getStandardExportPath(level);
        this.board = board;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerDir = playerDir;
    }

    public Path getOut() {
        return out;
    }

    public void setOut(Path out) {
        this.out = out;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = Objects.requireNonNull(board);
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
    }

    public Direction getPlayerDir() {
        return playerDir;
    }

    public void setPlayerDir(Direction playerDir) {
        this.playerDir = playerDir;
    }

    public boolean isDeadTiles() {
        return deadTiles;
    }

    public void setDeadTiles(boolean deadTiles) {
        this.deadTiles = deadTiles;
    }

    public boolean isRooms() {
        return rooms;
    }

    public void setRooms(boolean rooms) {
        this.rooms = rooms;
    }

    public boolean isTunnels() {
        return tunnels;
    }

    public void setTunnels(boolean tunnels) {
        this.tunnels = tunnels;
    }

    public boolean isLegend() {
        return legend;
    }

    public void setLegend(boolean legend) {
        this.legend = legend;
    }

    public boolean isXSB() {
        return xsb;
    }

    public void setXSB(boolean xsb) {
        this.xsb = xsb;
    }
}
