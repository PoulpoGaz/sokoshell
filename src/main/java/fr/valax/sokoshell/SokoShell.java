package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.jline.HelpCommand;
import fr.valax.args.jline.JLineUtils;
import fr.valax.args.jline.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.utils.LessCommand;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.AbstractWindowsTerminal;
import org.jline.widget.AutosuggestionWidgets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
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
            String initCommand = String.join(" ", args);

            sokoshell.loop(initCommand);
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

    private void loop(String initCommand) {
        Parser parser = new DefaultParser();

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
                    .build();

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            if (initCommand != null && !initCommand.isBlank()) {
                executeOrWaitInput(initCommand);
            }

            boolean running = true;
            while (running) {
                running = executeOrWaitInput(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean executeOrWaitInput(String line) {
        try {

            if (line == null) {
                line = reader.readLine(NAME + "> ");
            }

            cli.execute(line);
        } catch (EndOfFileException e) { // thrown when user types ctrl+D and by the built-in exit command
            return false;
        } catch (UserInterruptException e) {
            return false;
        } catch (IOException | CommandLineException e) {
            e.printStackTrace();
            return false;
        }

        return true;
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
                3, packs.size(), new String[] {"Pack", "Author", "Number of levels"},
                extractor);

        out.println(table);

        int totalLevels = 0;
        for (Pack p : packs) {
            totalLevels += p.levels().size();
        }

        out.println();
        out.printf("Total packs: %d - Total levels: %d%n", packs.size(), totalLevels);

        return Command.SUCCESS;
    }
}
