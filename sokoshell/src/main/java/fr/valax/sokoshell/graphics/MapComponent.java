package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.IBoard;

public class MapComponent extends Component {

    private Direction playerDir = Direction.DOWN;
    private IBoard<?> board;
    private int playerX;
    private int playerY;

    public MapComponent() {

    }

    @Override
    protected void drawComponent(Graphics g) {
        if (board != null) {
            BoardStyle style = SokoShell.INSTANCE.getBoardStyle();

            style.drawCenteredWithLegend(g, 0, 0, getWidth(), getHeight(), board, playerX, playerY, playerDir);
        }
    }

    public IBoard<?> getBoard() {
        return board;
    }

    public void setBoard(IBoard<?> board) {
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
