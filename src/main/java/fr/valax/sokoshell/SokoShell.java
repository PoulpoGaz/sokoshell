package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.jline.HelpCommand;
import fr.valax.args.jline.JLineUtils;
import fr.valax.args.jline.REPLHelpFormatter;
import fr.valax.args.utils.CommandLineException;
import fr.valax.sokoshell.commands.*;
import fr.valax.sokoshell.commands.level.PlayCommand;
import fr.valax.sokoshell.commands.level.SolutionCommand;
import fr.valax.sokoshell.commands.pack.SaveCommand;
import fr.valax.sokoshell.commands.select.Select;
import fr.valax.sokoshell.commands.select.SelectLevel;
import fr.valax.sokoshell.commands.select.SelectPack;
import fr.valax.sokoshell.commands.select.SelectStyle;
import fr.valax.sokoshell.commands.table.ListPacks;
import fr.valax.sokoshell.commands.table.ListSolution;
import fr.valax.sokoshell.commands.table.ListStyle;
import fr.valax.sokoshell.commands.table.ListTasks;
import fr.valax.sokoshell.commands.unix.*;
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
import org.jline.utils.AttributedStringBuilder;
import org.jline.widget.AutosuggestionWidgets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;

import static fr.valax.args.api.Command.SUCCESS;
import static org.jline.utils.AttributedStyle.*;

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
            SolverTask task = SokoShellHelper.INSTANCE.getRunningTask();
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

                .addCommand(new SolveCommand())    // the most important command!
                .addCommand(new StatusCommand())
                .addCommand(new PlayCommand())
                //.addCommand(new StatsCommand())
                .addCommand(new SaveCommand())

                .subCommand(new ListPacks())
                    .addCommand(new ListStyle())
                    .addCommand(new ListSolution())
                    .addCommand(new ListTasks())
                .endSubCommand()

                .subCommand(new PrintCommand())
                    .addCommand(new SolutionCommand())
                    .endSubCommand()

                .subCommand(new LoadCommand())
                    .addCommand(new LoadStyleCommand())
                    .endSubCommand()

                .subCommand(new Select())
                    .addCommand(new SelectPack())
                    .addCommand(new SelectLevel())
                    .addCommand(new SelectStyle())
                    .endSubCommand()

                // 'simple' commands
                .addCommand(AbstractCommand.newCommand(this::clear, "clear", "Clear screen"))
                .addCommand(AbstractCommand.newCommand(this::stopSolver, "stop", "Stop the solver"))
                .addCommand(AbstractCommand.newCommand(this::gc, "gc", "Run garbage collector.\nYou may want to use this after solving a sokoban"))

                // unix-like commands
                .addCommand(new Cat())
                .addCommand(new Echo())
                .addCommand(new Grep())
                .addCommand(new Less())
                .addCommand(new Source())
                .addCommand(help)
                .addCommand(JLineUtils.newExitCommand(NAME))
                .build();

        help.setCli(cli);
        helper.setCli(cli);
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
            helper.setTerminal(terminal);

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
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.styled(BOLD, NAME);

        if (helper.getSelectedPack() != null) {
            asb.append(" ");
            asb.styled(DEFAULT.foreground(GREEN + BRIGHT), helper.getSelectedPack().name());

            if (helper.getSelectedLevel() != null) {
                asb.append(":");
                asb.styled(DEFAULT.foreground(GREEN + BRIGHT), String.valueOf(helper.getSelectedLevelIndex() + 1));
            }
        }

        asb.append("> ");

        return asb.toAnsi();
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
        SolverTask task = helper.getRunningTask();

        if (task != null) {
            task.stop();
        }

        return SUCCESS;
    }

    private int gc(InputStream in, PrintStream out, PrintStream err) {
        Runtime.getRuntime().gc();

        return SUCCESS;
    }
}
