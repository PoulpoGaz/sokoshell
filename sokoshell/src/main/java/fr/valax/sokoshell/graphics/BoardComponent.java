package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;

public class BoardComponent extends Component {

    private Direction playerDir;
    private Board board;
    private int playerX;
    private int playerY;

    private boolean drawLegend = true;
    private boolean drawDeadTiles;
    private boolean drawRooms;
    private boolean drawTunnels;

    public BoardComponent() {

    }

    @Override
    protected void drawComponent(Graphics g) {
        if (board != null) {
            BoardStyle style = SokoShell.INSTANCE.getBoardStyle();

            boolean oldDeadTiles = style.isDrawDeadTiles();
            boolean oldRooms = style.isDrawRooms();
            boolean oldTunnel = style.isDrawTunnels();

            style.setDrawDeadTiles(drawDeadTiles);
            style.setDrawRooms(drawRooms);
            style.setDrawTunnels(drawTunnels);

            if (drawLegend) {
                style.drawCenteredWithLegend(g, 0, 0, getWidth(), getHeight(), board, playerX, playerY, playerDir);
            } else {
                style.drawCentered(g, 0, 0, getWidth(), getHeight(), board, playerX, playerY, playerDir);
            }

            style.setDrawDeadTiles(oldDeadTiles);
            style.setDrawRooms(oldRooms);
            style.setDrawTunnels(oldTunnel);
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        if (board != this.board) {
            this.board = board;
            repaint();
        }
    }

    public Direction getPlayerDir() {
        return playerDir;
    }

    public void setPlayerDir(Direction playerDir) {
        if (playerDir != this.playerDir) {
            this.playerDir = playerDir;
            repaint();
        }
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        if (playerX != this.playerX) {
            this.playerX = playerX;
            repaint();
        }
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        if (playerY != this.playerY) {
            this.playerY = playerY;
            repaint();
        }
    }

    public boolean isDrawLegend() {
        return drawLegend;
    }

    public void setDrawLegend(boolean showLegend) {
        if (showLegend != this.drawLegend) {
            this.drawLegend = showLegend;
            repaint();
        }
    }

    public boolean isDrawDeadTiles() {
        return drawDeadTiles;
    }

    public void setDrawDeadTiles(boolean drawDeadTiles) {
        if (this.drawDeadTiles != drawDeadTiles) {
            this.drawDeadTiles = drawDeadTiles;
            repaint();
        }
    }

    public boolean isDrawRooms() {
        return drawRooms;
    }

    public void setDrawRooms(boolean drawRooms) {
        if (this.drawRooms != drawRooms) {
            this.drawRooms = drawRooms;
            repaint();
        }
    }

    public boolean isDrawTunnels() {
        return drawTunnels;
    }

    public void setDrawTunnels(boolean drawTunnels) {
        if (this.drawTunnels != drawTunnels) {
            this.drawTunnels = drawTunnels;
            repaint();
        }
    }
}
