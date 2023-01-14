package fr.valax.sokoshell.graphics;

import fr.valax.sokoshell.SokoShell;
import fr.valax.sokoshell.graphics.style.MapRenderer;
import fr.valax.sokoshell.solver.Direction;
import fr.valax.sokoshell.solver.Map;

public class MapComponent extends Component {

    private Direction playerDir = Direction.DOWN;
    private Map map;
    private int playerX;
    private int playerY;

    public MapComponent() {

    }

    @Override
    protected void drawComponent(Graphics g) {
        if (map != null) {
            MapRenderer mr = SokoShell.INSTANCE.getRenderer();

            mr.drawCenteredWithLegend(g, 0, 0, getWidth(), getHeight(), map, playerX, playerY, playerDir);
        }
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        if (map != this.map) {
            this.map = map;
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
