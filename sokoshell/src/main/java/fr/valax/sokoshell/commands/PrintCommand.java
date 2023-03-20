package fr.valax.sokoshell.commands;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.api.TypeConverter;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.TypeException;
import fr.valax.interval.Set;
import fr.valax.sokoshell.Exporter;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.State;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.solver.board.MutableBoard;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.terminal.Size;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author PoulpoGaz
 */
public class PrintCommand extends AbstractCommand {

    @Option(names = {"p", "packs"}, hasArgument = true, argName = "Pack name", allowDuplicate = true)
    protected String[] packs;

    @Option(names = {"l", "levels"}, hasArgument = true, argName = "Levels")
    protected String levels;

    @Option(names = {"m", "maximize"}, description = "Equivalent to --size 1")
    protected boolean maximize;

    @Option(names = {"s", "size"}, hasArgument = true,
            description = "Float between 0 and 1. Draw the map with height equals to terminal height times size.",
            defaultValue = "-1")
    protected float size;

    @Option(names = {"d", "player-direction"}, hasArgument = true, converter = DirectionConverter.class)
    protected Direction playerDir;

    @Option(names = {"D", "draw-dead-tiles"})
    protected boolean drawDeadTiles;

    @Option(names = {"R", "draw-rooms"})
    protected boolean drawRooms;

    @Option(names = {"T", "draw-tunnels"})
    protected boolean drawTunnels;

    @Option(names = {"L", "legend"})
    protected boolean legend;

    @Option(names = {"f", "format"}, hasArgument = true, converter = FormatConverter.class)
    private String format;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Collection<Pack> packs = getPacks(this.packs);

        if (packs.isEmpty()) {
            return SUCCESS;
        }

        Set range = createSet(this.levels);

        for (Pack pack : packs) {
            out.printf("Pack: %s%n", pack.name());

            Iterator<Level> levels = getLevelMultipleIt(pack, range);

            while (levels.hasNext()) {
                Level l = levels.next();

                out.printf("<===== Level n°%d =====>%n", l.getIndex() + 1);
                process(l, out);
            }
        }

        return SUCCESS;
    }

    private void process(Level l, PrintStream out) {
        Board board = l;
        if (drawDeadTiles || drawRooms || drawTunnels) {
            State init = l.getInitialState();

            board = new MutableBoard(l);
            board.removeStateCrates(init);
            board.initForSolver();
            board.addStateCrates(init);
        }

        if (format == null) {
            print(out, board, l.getPlayerX(), l.getPlayerY());

        } else {
            export(out, l, board, l.getPlayerX(), l.getPlayerY());
        }
    }

    private void print(PrintStream out, Board board, int playerX, int playerY) {
        BoardStyle style = sokoshell().getBoardStyle();
        style.setDrawDeadTiles(drawDeadTiles);
        style.setDrawRooms(drawRooms);
        style.setDrawTunnels(drawTunnels);

        if (maximize) {
            size = 1;
        }

        Size s = sokoshell().getTerminal().getSize();

        int tileSize;
        if (size >= 0) {
            int availableWidth = (int) (s.getColumns() * size);
            int availableHeight = (int) (s.getRows() * size);

            tileSize = style.findBestSize(board, availableWidth, availableHeight);
        } else {
            tileSize = style.findBestSize(0);

        }

        Surface surface = new Surface();
        surface.resize(board.getWidth() * tileSize, board.getHeight() * tileSize);

        Graphics g = new Graphics(surface);
        if (maximize) {
            style.drawWithLegend(g, tileSize, board, playerX, playerY, playerDir);
        } else {
            style.draw(g, tileSize, board, playerX, playerY, playerDir);
        }

        surface.print(out);
    }

    private void export(PrintStream out, Level level, Board board, int playerX, int playerY) {
        Exporter exporter = new Exporter();
        exporter.setOut(sokoshell().getStandardExportPath(level));
        exporter.setBoard(board);
        exporter.setPlayerX(playerX);
        exporter.setPlayerY(playerY);
        exporter.setPlayerDir(playerDir);
        exporter.setDeadTiles(drawDeadTiles);
        exporter.setTunnels(drawTunnels);
        exporter.setRooms(drawRooms);
        exporter.setLegend(legend);
        exporter.setXSB(format.equals("xsb"));

        Path pOut = exporter.silentExport();
        if (pOut != null) {
            out.printf("Exported to %s%n", pOut);
        }
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getShortDescription() {
        return "print a state";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option != null) {
            if (ArgsUtils.contains(option.getShortNames(), 'p')) {
                sokoshell().addPackCandidates(candidates);
            } else if (ArgsUtils.contains(option.getShortNames(), 'd')) {
                candidates.add(new Candidate("left"));
                candidates.add(new Candidate("right"));
                candidates.add(new Candidate("up"));
                candidates.add(new Candidate("down"));
            } else if (ArgsUtils.contains(option.getShortNames(), 'f')) {
                candidates.add(new Candidate("png"));
                candidates.add(new Candidate("xsb"));
            }
        }
    }

    private static class DirectionConverter implements TypeConverter<Direction> {

        @Override
        public Direction convert(String value) throws TypeException {
            if (value == null) {
                return null;
            } else {
                return switch (value.toLowerCase()) {
                    case "up",    "u" -> Direction.UP;
                    case "left",  "l" -> Direction.LEFT;
                    case "down",  "d" -> Direction.DOWN;
                    case "right", "r" -> Direction.RIGHT;
                    default -> throw new TypeException("Unknown direction: " + value);
                };
            }
        }
    }

    private static class FormatConverter implements TypeConverter<String> {

        @Override
        public String convert(String value) throws TypeException {
            if (value == null) {
                return null;
            } else if (value.equalsIgnoreCase("png")) {
                return "png";
            } else if (value.equalsIgnoreCase("xsb")) {
                return "xsb";
            } else {
                throw new TypeException("Unknown format: " + value);
            }
        }
    }
}
