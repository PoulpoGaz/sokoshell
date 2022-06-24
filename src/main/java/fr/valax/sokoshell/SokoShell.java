package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.repl.REPLCommandRegistry;
import fr.valax.args.repl.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.ParseException;
import fr.valax.args.utils.TypeException;
import fr.valax.sokoshell.solver.Pack;
import fr.valax.sokoshell.solver.tasks.ISolverTask;
import fr.valax.sokoshell.utils.PrettyTable;
import fr.valax.sokoshell.utils.Utils;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.AbstractWindowsTerminal;
import org.jline.widget.AutosuggestionWidgets;

import javax.security.auth.callback.PasswordCallback;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author PoulpoGaz
 */
public class SokoShell {

    public static final String VERSION = "0.1";

    public static void main(String[] args) {
        SokoShell sokoshell;
        try {
            sokoshell = new SokoShell();
        } catch (CommandLineException e) {
            throw new IllegalStateException("Failed to initialize CLI", e);
        }

        sokoshell.welcome();

        try {
            if (args.length > 0) {
                sokoshell.execute(args);
            }

            sokoshell.loop();
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
                .build();
    }

    private void welcome() {
        System.out.printf("""
                Welcome to sokoshell - Version %s
                Type 'help' to show help. More help for a command with 'help command'
                """, VERSION);
    }

    private void goodbye() {
        System.out.println("Goodbye!");
    }

    private void loop() {
        Parser parser = new DefaultParser();
        REPLCommandRegistry shellRegistry = new REPLCommandRegistry(cli, "SokoShell");

        try (Terminal terminal = TerminalBuilder.terminal()) {
            this.terminal = terminal;

            if (terminal instanceof AbstractWindowsTerminal) {
                System.err.println("[WARNING]. Your terminal isn't supported");
            }

            SokoShellHelper.INSTANCE.setTerminal(terminal);

            SystemRegistry registry = new SystemRegistryImpl(parser, terminal, () -> Path.of(""), null);
            registry.setCommandRegistries(shellRegistry);

            reader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName("sokoshell")
                    .history(new DefaultHistory())
                    .highlighter(new DefaultHighlighter())
                    .parser(parser)
                    .completer(shellRegistry.compileCompleters())
                    .variable(LineReader.HISTORY_FILE, getHistoryPath())
                    .build();

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            while (true) {
                registry.cleanUp();

                try {
                    String line = reader.readLine("sokoshell> ");
                    registry.execute(line);
                } catch (EndOfFileException e) { // thrown when user types ctrl+D and by the built-in exit command
                    break;
                } catch (UserInterruptException e) {
                    break;
                } catch (SystemRegistryImpl.UnknownCommandException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void execute(String[] args) {
        try {
            System.out.println("sokoshell> " + String.join(" ", args));
            cli.execute(args);

        } catch (ParseException | TypeException e) {
            System.out.println(e.getMessage());
        }  catch (CommandLineException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getHistoryPath() {
        String home = System.getProperty("user.home");

        return Path.of(home + "/.sokoshell_history");
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

        System.out.println(table);

        int totalLevels = 0;
        for (Pack p : packs) {
            totalLevels += p.levels().size();
        }

        System.out.println();
        System.out.printf("Total packs: %d - Total levels: %d%n", packs.size(), totalLevels);

        return Command.SUCCESS;
    }
}
