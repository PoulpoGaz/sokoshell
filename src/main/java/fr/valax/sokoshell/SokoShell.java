package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.jline.HelpCommand;
import fr.valax.args.jline.JLineUtils;
import fr.valax.args.jline.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.graphics.MapStyle;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.utils.*;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static fr.valax.args.api.Command.SUCCESS;

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
                    .addCommand(new ListSolutionCommand())
                .endSubCommand()
                .addCommand(new LoadStyleCommand())
                .addCommand(new SetStyleCommand())
                .addCommand(new LessCommand())
                .addCommand(JLineUtils.newExitCommand(NAME))
                .addCommand(help)
                .addCommand(new BasicCommands.Cat())
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

    private static class ListPacks extends TableCommand {

        @Override
        protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
            List<Pack> packs = helper.getPacks().stream()
                    .sorted(Comparator.comparing(Pack::name))
                    .toList();

            PrettyTable2 table = new PrettyTable2();

            PrettyColumn<String> name = new PrettyColumn<>("Name");
            PrettyColumn<String> author = new PrettyColumn<>("Author");
            PrettyColumn<Integer> version = new PrettyColumn<>("Number of levels");

            for (Pack pack : packs) {
                name.add(pack.name());
                author.add(pack.author());
                version.add(Alignment.RIGHT, pack.levels().size());
            }

            table.addColumn(name);
            table.addColumn(author);
            table.addColumn(version);

            printTable(out, err, table);

            int totalLevels = 0;
            for (Pack p : packs) {
                totalLevels += p.levels().size();
            }
            out.printf("%nTotal packs: %d - Total levels: %d%n", packs.size(), totalLevels);

            return 0;
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

    private static class ListStyle extends TableCommand {

        @Override
        public String getName() {
            return "style2";
        }

        @Override
        public String getShortDescription() {
            return null;
        }

        @Override
        public String[] getUsage() {
            return new String[0];
        }

        @Override
        protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
            MapStyle selected = helper.getMapStyle();

            List<MapStyle> mapStyles = helper.getMapStyles().stream()
                    .sorted(Comparator.comparing(MapStyle::getName))
                    .toList();

            PrettyTable2 table = new PrettyTable2();

            PrettyColumn<AttributedString> name = new PrettyColumn<>("name");
            name.setToString((s) -> new AttributedString[] {s});
            name.setComparator(Utils.ATTRIBUTED_STRING_COMPARATOR);

            PrettyColumn<String> author = new PrettyColumn<>("author");
            PrettyColumn<String> version = new PrettyColumn<>("version");

            for (MapStyle style : mapStyles) {
                if (selected == style) {
                    name.add(new AttributedString("* " +style.getName() + " *" , AttributedStyle.BOLD));
                } else {
                    name.add(new AttributedString(style.getName()));
                }

                author.add(style.getAuthor());
                version.add(style.getVersion());
            }

            table.addColumn(name);
            table.addColumn(author);
            table.addColumn(version);

            printTable(out, err, table);

            return 0;
        }
    }
}
