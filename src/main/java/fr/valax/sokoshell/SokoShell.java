package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.jline.HelpCommand;
import fr.valax.args.jline.JLineUtils;
import fr.valax.args.jline.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.Level;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.Solution;
import fr.valax.sokoshell.solver.SolverStatistics;
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
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import static fr.valax.args.api.Command.*;

/**
 * @author PoulpoGaz
 */
public class SokoShell {

    public static final String VERSION = "0.1";
    public static final String NAME = "sokoshell";

    public static final Path USER_HOME = Path.of(System.getProperty("user.home"));
    public static final Path HISTORY = USER_HOME.resolve(".%s_history".formatted(NAME));
    public static final Path RUN_COMMAND = USER_HOME.resolve(".%src".formatted(NAME));

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
                .subCommand(new ListPacks())
                    .addCommand(new ListStyle())
                    .addCommand(new ListSolution())
                .endSubCommand()
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

    // READING AND EXECUTING

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
                    .variable(LineReader.HISTORY_FILE, HISTORY)
                    .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                    .build();

            if (!executeStartupScript()) {
                return;
            }

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

    // STARTUP SCRIPT

    private boolean executeStartupScript() {
        StartupScript ss = new StartupScript(cli);
        try {
            ss.run(RUN_COMMAND);

            return true;
        } catch (IOException | CommandLineException e) {
            System.err.println("Failed to run startup script");
            e.printStackTrace();
            return false;
        }
    }

    // BASIC COMMANDS

    private int clear(InputStream in, PrintStream out, PrintStream err) {
        if (reader != null) {
            reader.clearScreen();
        }

        return SUCCESS;
    }

    private int stopSolver(InputStream in, PrintStream out, PrintStream err) {
        ISolverTask<?> task = helper.getSolverTask();

        if (task != null) {
            task.stop();
        }

        return SUCCESS;
    }

    private int gc(InputStream in, PrintStream out, PrintStream err) {
        Runtime.getRuntime().gc();

        return SUCCESS;
    }

    private static class ListPacks extends TableCommand<Pack> {

        private List<Pack> packs;

        @Override
        protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
            packs = helper.getPacks().stream()
                    .sorted(Comparator.comparing(Pack::name))
                    .toList();
            printTable(out, packs);

            return 0;
        }

        @Override
        protected String[] getHeaders() {
            return new String[] {"Pack", "Author", "Number of levels"};
        }

        @Override
        protected PrettyTable.Cell extract(Pack pack, int x) {
            return switch (x) {
                case 0 -> new PrettyTable.Cell(pack.name());
                case 1 -> new PrettyTable.Cell(pack.author());
                case 2 -> new PrettyTable.Cell(String.valueOf(pack.levels().size()));
                default -> throw new IllegalArgumentException();
            };
        }

        @Override
        protected String countLine() {
            int totalLevels = 0;
            for (Pack p : packs) {
                totalLevels += p.levels().size();
            }

            return "Total packs: %d - Total levels: " + totalLevels + "%n";
        }

        @Override
        protected String whenEmpty() {
            return "No pack loaded";
        }

        @Override
        public String getName() {
            return "list";
        }

        @Override
        public String getShortDescription() {
            return "List all packs";
        }

        @Override
        public String[] getUsage() {
            return new String[0];
        }
    }

    private static class ListStyle extends TableCommand<MapStyle> {

        private MapStyle selected;

        @Override
        protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
            selected = helper.getMapStyle();

            List<MapStyle> mapStyles = helper.getMapStyles().stream()
                    .sorted(Comparator.comparing(MapStyle::getName))
                    .toList();

            printTable(out, mapStyles);

            return 0;
        }

        @Override
        protected String[] getHeaders() {
            return new String[] {"Name", "Author", "Version"};
        }

        @Override
        protected PrettyTable.Cell extract(MapStyle mapStyle, int x) {
            return switch (x) {
                case 0 -> {
                    if (mapStyle == selected) {
                        AttributedString str = new AttributedString("* " + mapStyle.getName() + " *", AttributedStyle.BOLD);
                        yield new PrettyTable.Cell(str);
                    } else {
                        yield new PrettyTable.Cell(mapStyle.getName());
                    }
                }
                case 1 -> new PrettyTable.Cell(mapStyle.getAuthor());
                case 2 -> new PrettyTable.Cell(String.valueOf(mapStyle.getVersion()));
                default -> throw new IllegalArgumentException();
            };
        }

        @Override
        protected String countLine() {
            return "Total map styles: %d%n";
        }

        @Override
        protected String whenEmpty() {
            throw new IllegalArgumentException();
        }

        @Override
        public String getName() {
            return "style";
        }

        @Override
        public String getShortDescription() {
            return "List all styles";
        }

        @Override
        public String[] getUsage() {
            return new String[0];
        }
    }
}
