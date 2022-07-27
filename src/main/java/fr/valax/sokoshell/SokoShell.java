package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.jline.HelpCommand;
import fr.valax.args.jline.JLineUtils;
import fr.valax.args.jline.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.utils.LessCommand;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.AbstractWindowsTerminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.widget.AutosuggestionWidgets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author PoulpoGaz
 */
public class SokoShell {

    public static final String VERSION = "0.1";
    public static final String NAME = "sokoshell";

    public static void main(String[] args) {
        SokoShell sokoshell;
        try {
            sokoshell = new SokoShell();
        } catch (CommandLineException e) {
            throw new IllegalStateException("Failed to initialize CLI", e);
        }

        sokoshell.welcome();

        try {
            sokoshell.loop(args);
        } finally {
            ISolverTask<?> task = SokoShellHelper.INSTANCE.getSolverTask();
            if (task != null) {
                task.stop();
            }

            Utils.shutdownExecutors();

            sokoshell.goodbye();
        }
    }

    private final CommandLine cli;
    private final SokoShellHelper helper = SokoShellHelper.INSTANCE;
    private LineReaderImpl reader;
    private Terminal terminal;

    private SokoShell() throws CommandLineException {
        HelpCommand help = new HelpCommand();

        cli = new CommandLineBuilder()
                .addDefaultConverters()
                .setHelpFormatter(new REPLHelpFormatter())
                .addCommand(new SolveCommand())
                .addCommand(new BenchmarkCommand())
                .subCommand(new PrintCommand())
                    .addCommand(new SolutionCommand())
                    .endSubCommand()
                .addCommand(new LoadCommand())
                .addCommand(new StatusCommand())
                .addCommand(new PlayCommand())
                .addCommand(new StatsCommand())
                .addCommand(new SaveCommand())
                .addCommand(AbstractCommand.newCommand(this::clear, "clear", "Clear screen"))
                .addCommand(AbstractCommand.newCommand(this::stopSolver, "stop", "Stop the solver"))
                .addCommand(AbstractCommand.newCommand(this::gc, "gc", "Run garbage collector.\nYou may want to use this after solving a sokoban"))
                .addCommand(AbstractCommand.newCommand(this::list, "list", "List all packs"))
                .addCommand(AbstractCommand.newCommand(this::listStyle, "list-style", "List all styles"))
                .addCommand(new LoadStyleCommand())
                .addCommand(new SetStyleCommand())
                .addCommand(new LessCommand())
                .addCommand(JLineUtils.newExitCommand(NAME))
                .addCommand(help)
                .build();

        help.setCli(cli);
        cli.setName(NAME);
    }

    private void welcome() {
        System.out.printf("""
                Welcome to %s - Version %s
                Type 'help' to show help. More help for a command with 'help command'
                """, NAME, VERSION);
    }

    private void goodbye() {
        System.out.println("Goodbye!");
    }

    private void loop(String[] args) {
        DefaultParser parser = new DefaultParser();

        try (Terminal terminal = TerminalBuilder.terminal()) {
            this.terminal = terminal;

            if (terminal instanceof AbstractWindowsTerminal) {
                System.err.println("[WARNING]. Your terminal isn't supported");
            }

            cli.setStdIn(terminal.input());
            cli.setStdOut(new PrintStream(terminal.output()));

            SokoShellHelper.INSTANCE.setTerminal(terminal);

            reader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName(NAME)
                    .history(new DefaultHistory())
                    .highlighter(new DefaultHighlighter())
                    .parser(parser)
                    .completer(JLineUtils.createCompleter(cli))
                    .variable(LineReader.HISTORY_FILE, getHistoryPath())
                    .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                    .build();

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            boolean running = true;
            while (running) {
                running = executeOrWaitInput(args);
                args = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args program args or null
     * @return false to stop
     */
    private boolean executeOrWaitInput(String[] args) {
        boolean reading = false;
        try {

            if (args == null || args.length == 0) {
                reading = true;
                String line = reader.readLine(getPrompt());
                reading = false;

                cli.execute(line);
            } else {
                System.out.println(getPrompt() + Arrays.toString(args));
                cli.execute(args);
            }
        } catch (EndOfFileException e) { // thrown when user types ctrl+D and by the built-in exit command
            return false;
        } catch (UserInterruptException e) {
            return !reading;
        } catch (IOException | CommandLineException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String getPrompt() {
        return new AttributedString(NAME + "> ", AttributedStyle.BOLD).toAnsi();
    }

    private Path getHistoryPath() {
        String home = System.getProperty("user.home");

        return Path.of("%s/.%s_history".formatted(home, NAME));
    }

    private int clear(InputStream in, PrintStream out, PrintStream err) {
        if (reader != null) {
            reader.clearScreen();
        }

        return Command.SUCCESS;
    }

    private int stopSolver(InputStream in, PrintStream out, PrintStream err) {
        ISolverTask<?> task = helper.getSolverTask();

        if (task != null) {
            task.stop();
        }

        return Command.SUCCESS;
    }

    private int gc(InputStream in, PrintStream out, PrintStream err) {
        Runtime.getRuntime().gc();

        return Command.SUCCESS;
    }

    private int list(InputStream in, PrintStream out, PrintStream err) {
        List<Pack> packs = helper.getPacks().stream()
                .sorted(Comparator.comparing(Pack::name))
                .toList();

        int totalLevels = 0;

        if (packs.size() > 0) {
            BiFunction<Integer, Integer, PrettyTable.Cell> extractor = (x, y) -> {
                Pack pack = packs.get(y);

                return switch (x) {
                    case 0 -> new PrettyTable.Cell(pack.name());
                    case 1 -> new PrettyTable.Cell(pack.author());
                    case 2 -> new PrettyTable.Cell(String.valueOf(pack.levels().size()));
                    default -> throw new IllegalArgumentException();
                };
            };

            String table = PrettyTable.create(
                    3, packs.size(), new String[]{"Pack", "Author", "Number of levels"},
                    extractor);

            out.println(table);
            out.println();
        }

        for (Pack p : packs) {
            totalLevels += p.levels().size();
        }

        out.printf("Total packs: %d - Total levels: %d%n", packs.size(), totalLevels);

        return Command.SUCCESS;
    }

    private Integer listStyle(InputStream in, PrintStream out, PrintStream err) {
        MapStyle selected = helper.getMapStyle();

        List<MapStyle> mapStyles = helper.getMapStyles().stream()
                .sorted(Comparator.comparing(MapStyle::getName))
                .toList();

        if (mapStyles.size() > 0) {
            BiFunction<Integer, Integer, PrettyTable.Cell> extractor = (x, y) -> {
                MapStyle mapStyle = mapStyles.get(y);

                return switch (x) {
                    case 0 -> {
                        if (mapStyle == selected) {
                            yield new PrettyTable.Cell("* " + mapStyle.getName() + " *");
                        } else {
                            yield new PrettyTable.Cell(mapStyle.getName());
                        }
                    }
                    case 1 -> new PrettyTable.Cell(mapStyle.getAuthor());
                    case 2 -> new PrettyTable.Cell(String.valueOf(mapStyle.getVersion()));
                    default -> throw new IllegalArgumentException();
                };
            };

            String table = PrettyTable.create(
                    3, mapStyles.size(), new String[] {"Name", "Author", "Version"},
                    extractor);

            out.println(table);
            out.println();
        }

        out.printf("Total map styles: %d%n", mapStyles.size());

        return Command.SUCCESS;
    }
}
