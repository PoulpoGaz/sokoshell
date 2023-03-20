package fr.valax.sokoshell.graphics.export;

import fr.valax.sokoshell.Exporter;
import fr.valax.sokoshell.graphics.*;
import fr.valax.sokoshell.graphics.layout.BorderLayout;
import fr.valax.sokoshell.graphics.layout.VerticalConstraint;
import fr.valax.sokoshell.graphics.layout.VerticalLayout;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;

import java.nio.file.Path;

public class ExportPopup extends fr.valax.sokoshell.graphics.Component {

    public static ExportPopup show(TerminalEngine engine) {
        fr.valax.sokoshell.graphics.Component root = engine.getRootComponent();

        ExportPopup popup = new ExportPopup(root);
        engine.setRootComponent(popup);

        return popup;
    }

    private final Component original;

    private Path out;
    private Board board;
    private int playerX;
    private int playerY;
    private Direction playerDir;

    private Group group;
    private TextButton deadTiles;
    private TextButton rooms;
    private TextButton tunnels;
    private TextButton legend;
    private SelectButton direction;
    private TextButton xsb;

    private BoardComponent preview;

    public ExportPopup() {
        this(null);
    }

    public ExportPopup(Component original) {
        this.original = original;
        initComponent();
    }

    private void initComponent() {
        setLayout(new BorderLayout());

        preview = new BoardComponent();
        preview.setDrawLegend(true);
        preview.setDrawDeadTiles(true);
        preview.setDrawTunnels(true);
        preview.setDrawRooms(true);

        BiButton export = new BiButton("Export", "Cancel");
        export.addActionListener((o, c) -> export(c.equals("left")));
        export.setLeftSelected(true);

        add(preview, BorderLayout.CENTER);
        add(createChoicePanel(), BorderLayout.EAST);
        add(export, BorderLayout.SOUTH);
        add(createTitle(), BorderLayout.NORTH);
    }

    private Component createTitle() {
        return new MultilineLabel("""
                Use space to select, arrow keys to move, enter to cancel or export.
                Warning: exporting to XSB with one of the three first options add some LaTeX after the level.
                """);
    }

    private Component createChoicePanel() {
        deadTiles = new TextButton("Dead tiles ?");
        deadTiles.setSelected(true);
        deadTiles.addActionListener((o, s) -> preview.setDrawDeadTiles(!preview.isDrawDeadTiles()));

        rooms = new TextButton("Rooms ?");
        rooms.setSelected(true);
        rooms.addActionListener((o, s) -> preview.setDrawRooms(!preview.isDrawRooms()));

        tunnels = new TextButton("Tunnels ?");
        tunnels.setSelected(true);
        tunnels.addActionListener((o, s) -> preview.setDrawTunnels(!preview.isDrawTunnels()));

        legend = new TextButton("Legend ?");
        legend.setSelected(true);
        legend.addActionListener((o, s) -> preview.setDrawLegend(!preview.isDrawLegend()));

        xsb = new TextButton("XSB ?");
        xsb.setSelected(true);

        direction = createDirectionComponent();

        group = new Group();
        group.addComponent(deadTiles);
        group.addComponent(rooms);
        group.addComponent(tunnels);
        group.addComponent(legend);
        group.addComponent(xsb);
        group.addComponent(direction);

        Component component = new Component();
        component.setBorder(new BasicBorder());
        component.setLayout(new VerticalLayout());

        VerticalConstraint constraint = new VerticalConstraint();
        constraint.xAlignment = 0;
        component.add(deadTiles, constraint);
        component.add(rooms, constraint);
        component.add(tunnels, constraint);
        component.add(legend, constraint);
        component.add(xsb, constraint);
        component.add(NamedComponent.create("Direction:", direction), constraint);

        return component;
    }

    @Override
    protected void updateComponent() {
        if (keyPressed(Key.UP)) {
            group.focusPrevious();
        }
        if (keyPressed(Key.DOWN)) {
            group.focusNext();
        }
        if (keyPressed(Key.ESCAPE)) {
            export(false);
        }
    }

    private SelectButton createDirectionComponent() {
        SelectButton direction = new SelectButton();
        direction.addChoice("None");
        direction.addChoice("Up");
        direction.addChoice("Left");
        direction.addChoice("Down");
        direction.addChoice("Right");
        direction.addActionListener((o, s) -> {
            Direction dir = switch (direction.getChoice().toAnsi()) {
                case "None" -> null;
                case "Up" -> Direction.UP;
                case "Left" -> Direction.LEFT;
                case "Down" -> Direction.DOWN;
                case "Right" -> Direction.RIGHT;
                default -> throw new IllegalStateException();
            };

            playerDir = dir;
            preview.setPlayerDir(dir);
        });

        return direction;
    }

    private void export(boolean export) {
        if (export) {
            Exporter exporter = new Exporter();
            exporter.setBoard(board);
            exporter.setPlayerX(playerX);
            exporter.setPlayerY(playerY);
            exporter.setPlayerDir(playerDir);
            exporter.setDeadTiles(deadTiles.isSelected());
            exporter.setRooms(rooms.isSelected());
            exporter.setTunnels(tunnels.isSelected());
            exporter.setLegend(legend.isSelected());
            exporter.setXSB(xsb.isSelected());
            exporter.setOut(out);

            fireExportDone(exporter.silentExport());
        } else {
            fireExportCanceled();
        }

        if (original != null) {
            getEngine().setRootComponent(original);
        }
    }

    private void fireExportDone(Path out) {
        for (ExportListener listener : getExportListeners()) {
            listener.exportDone(out);
        }
    }

    private void fireExportCanceled() {
        for (ExportListener listener : getExportListeners()) {
            listener.exportCanceled();
        }
    }

    public void addExportListener(ExportListener listener) {
        listeners.add(ExportListener.class, listener);
    }

    public void removeExportListener(ExportListener listener) {
        listeners.remove(ExportListener.class, listener);
    }

    public ExportListener[] getExportListeners() {
        return listeners.getListeners(ExportListener.class);
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
        this.board = board;
        preview.setBoard(board);
    }

    public int getPlayerX() {
        return playerX;
    }

    public void setPlayerX(int playerX) {
        this.playerX = playerX;
        preview.setPlayerX(playerX);
    }

    public int getPlayerY() {
        return playerY;
    }

    public void setPlayerY(int playerY) {
        this.playerY = playerY;
        preview.setPlayerY(playerY);
    }

    public Direction getPlayerDir() {
        return playerDir;
    }

    public void setPlayerDir(Direction playerDir) {
        this.playerDir = playerDir;
        preview.setPlayerDir(playerDir);

        if (playerDir == null) {
            direction.setChoice(0);
        } else {
            switch (playerDir) {
                case UP -> direction.setChoice(1);
                case LEFT -> direction.setChoice(2);
                case DOWN -> direction.setChoice(3);
                case RIGHT -> direction.setChoice(4);
            }
        }
    }
}
