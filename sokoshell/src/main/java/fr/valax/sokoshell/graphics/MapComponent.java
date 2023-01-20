package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShellHelper;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Board;

public class MapComponent extends Component {

    private Direction playerDir = Direction.DOWN;
    private Board board;
    private int playerX;
    private int playerY;

    public MapComponent() {

    }

    @Override
    protected void drawComponent(Graphics g) {
        if (board != null) {
            MapRenderer mr = SokoShellHelper.INSTANCE.getRenderer();

            mr.draw(g, 0, 0, getWidth(), getHeight(), board, playerX, playerY, playerDir);
        }
    }

    public Board getMap() {
        return board;
    }

    public void setMap(Board board) {
        if (board != this.board) {
            this.board = board;
            repaint();
        }
    }

    public Direction getPlayerDir() {
        return playerDir;
    }

    public void setPlayerDir(Direction playerDir) {
        if (playerDir != null && playerDir != this.playerDir) {
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
}
