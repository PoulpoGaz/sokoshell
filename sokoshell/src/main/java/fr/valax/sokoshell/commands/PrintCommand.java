package fr.valax.sokoshell.commands;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.interval.Set;
import fr.valax.sokoshell.graphics.Graphics;
import fr.valax.sokoshell.graphics.Surface;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.board.Direction;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.terminal.Size;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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

    @Option(names = {"m", "maximize"})
    protected boolean maximize;

    @Option(names = {"d", "player-direction"}, hasArgument = true)
    protected String direction;

    @Option(names = {"e", "export"})
    private boolean export;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Direction playerDir = Direction.DOWN;

        if (direction != null) {
            playerDir = parseDirection(direction);
        }

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

                if (maximize) {
                    Size s = sokoshell().getTerminal().getSize();

                    Surface surface = new Surface();
                    surface.resize(s.getColumns(), s.getRows());
                    Graphics g = new Graphics(surface);
                    sokoshell().getBoardStyle().drawCenteredWithLegend(g, 0, 0, s.getColumns(), s.getRows(), l, l.getPlayerX(), l.getPlayerY(), playerDir);
                    surface.print(out);
                } else {
                    sokoshell().getBoardStyle().print(out, l);
                }

                if (export) {
                    /*try {
                        sokoshell().exportPNG(l.getPack(), l, l, l.getPlayerX(), l.getPlayerY(), playerDir);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                }
            }
        }

        return SUCCESS;
    }

    private Direction parseDirection(String direction) throws InvalidArgument {
        if (direction.equalsIgnoreCase("left") || direction.equalsIgnoreCase("l")) {
            return Direction.LEFT;
        } else if (direction.equalsIgnoreCase("right") || direction.equalsIgnoreCase("r")) {
            return Direction.RIGHT;
        } else if (direction.equalsIgnoreCase("up") || direction.equalsIgnoreCase("u")) {
            return Direction.UP;
        } else if (direction.equalsIgnoreCase("down") || direction.equals("d")) {
            return Direction.DOWN;
        } else {
            throw new InvalidArgument("Unknown direction: " + direction);
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
            }
        }
    }
}
