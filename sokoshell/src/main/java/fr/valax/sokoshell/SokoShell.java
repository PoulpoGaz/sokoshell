package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
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
import fr.valax.sokoshell.commands.table.*;
import fr.valax.sokoshell.commands.unix.*;
import fr.valax.sokoshell.graphics.style.BasicStyle;
import fr.valax.sokoshell.graphics.style.BoardStyle;
import fr.valax.sokoshell.solver.*;
import fr.valax.sokoshell.solver.board.Board;
import fr.valax.sokoshell.solver.board.Direction;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.impl.AbstractWindowsTerminal;
import org.jline.terminal.impl.DumbTerminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.widget.AutosuggestionWidgets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static fr.valax.args.api.Command.SUCCESS;
import static org.jline.utils.AttributedStyle.*;

/**
 * Entry point of sokoshell. It executes the startup script,
 * reads lines and executes command.
 *
 * @author PoulpoGaz
 */
public class SokoShell {

    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String NAME = "sokoshell";

    public static final Path USER_HOME = Path.of(System.getProperty("user.home"));
    public static final Path HISTORY = USER_HOME.resolve(".%s_history".formatted(NAME));

    public static final Path EXPORT_FOLDER = Path.of("export");
    public static final Path ERROR_FILE = Path.of("errors");

    public static final SokoShell INSTANCE = new SokoShell();

    public static void main(String[] args) {
        boolean noPrompt = false;

        Path sokoshellrc = USER_HOME.resolve(".%src".formatted(NAME));
        for (String str : args) {
            if (str.equals("--no-prompt") || str.equals("-n")) {
                noPrompt = true;
            } else {
                sokoshellrc = Path.of(args[0]);
            }
        }

        SokoShell.INSTANCE.run(sokoshellrc, noPrompt);
    }




    private final CommandLine cli;

    private LineReaderImpl reader;
    private Terminal terminal;

    private boolean promptEnabled;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledExecutor =
            Executors.newScheduledThreadPool(
                    Math.max(1, Runtime.getRuntime().availableProcessors() / 4));



    private final java.util.Map<String, Solver> solvers = new HashMap<>();
    private final java.util.Map<String, Pack> packs = new HashMap<>();
    private final java.util.Map<String, BoardStyle> styles = new HashMap<>();

    private final TaskList taskList;

    private INotificationHandler notificationHandler;

    private Pack selectedPack = null;
    private int selectedLevel = -1;
    private BoardStyle selectedStyle;

    private boolean autoSaveSolution = false;

    private boolean isShutdown;


    private SokoShell() {
        cli = createCommandLine();

        addSolver(BruteforceSolver.newBFSSolver());
        addSolver(BruteforceSolver.newDFSSolver());
        addSolver(new AStarSolver());
        addSolver(new FESS0Solver());

        addBoardStyle(BasicStyle.DEFAULT_STYLE);
        addBoardStyle(BasicStyle.XSB_STYLE);
        selectedStyle = BasicStyle.DEFAULT_STYLE;

        taskList = new TaskList();
    }


    private CommandLine createCommandLine() {
        try {
            HelpCommand help = new HelpCommand();

            CommandLine cli = new CommandLineBuilder()
                    .addDefaultConverters()
                    .setHelpFormatter(new REPLHelpFormatter())

                    .addCommand(new TestCommand())
                    .addCommand(new SolveCommand())    // the most important command!
                    .addCommand(new CancelCommand())
                    .addCommand(new MonitorCommand())
                    .addCommand(new PlayCommand())
                    .addCommand(new StatsCommand())
                    .addCommand(new SaveCommand())
                    .addCommand(new AutoSaveSolutionCommand())
                    .addCommand(new MoveTaskCommand())
                    .addCommand(new RemoveReport())

                    .subCommand(new ListPacks())
                        .addCommand(new ListStyle())
                        .addCommand(new ListReports())
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
                    .addCommand(AbstractCommand.newCommand(this::gc, "gc", "Run garbage collector.\nYou may want to use this after solving a sokoban"))

                    .addCommand(new ObjectSizeCommand())

                    // unix-like commands
                    .addCommand(new Cat())
                    .addCommand(new Echo())
                    .addCommand(new Grep())
                    .addCommand(new Less())
                    .addCommand(new Source())
                    .addCommand(new WordCount())
                    .addCommand(help)
                    .addCommand(JLineUtils.newExitCommand(NAME))
                    .build();

            help.setCli(cli);
            cli.setName(NAME);

            return cli;
        } catch (CommandLineException e) {
            throw new IllegalStateException(e);
        }
    }

    // READING AND EXECUTING

    private void run(Path sokoshellrc, boolean noPrompt) {
        promptEnabled = !noPrompt;
        if (noPrompt) {
            cli.setStdIn(System.in);
            cli.setStdOut(System.out);

            try {
                executeStartupScript(sokoshellrc);
            } finally {
                shutdown();
            }
        } else {
            loop(sokoshellrc);
        }
    }

    /**
     * Initialize terminal, command line and line reader.
     * Execute startup script
     *
     * @param sokoshellrc sokoshellrc location
     */
    private void loop(Path sokoshellrc) {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            this.terminal = terminal;

            if (terminal instanceof AbstractWindowsTerminal || terminal instanceof DumbTerminal) {
                System.err.println("[WARNING]. Your terminal isn't supported");
            }

            cli.setStdIn(terminal.input());
            cli.setStdOut(new PrintStream(terminal.output()));

            reader = (LineReaderImpl) LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName(NAME)
                    .history(new DefaultHistory())
                    .highlighter(new DefaultHighlighter())
                    .parser(new DefaultParser())
                    .completer(JLineUtils.createCompleter(cli))
                    .variable(LineReader.HISTORY_FILE, HISTORY)
                    .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                    .build();

            welcome();
            if (!executeStartupScript(sokoshellrc)) {
                return;
            }

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            boolean running = true;
            while (running) {
                running = waitInputAndExecute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Print welcome message
     */
    private void welcome() {
        terminal.writer().printf("""
                Welcome to %s - Version %s
                Type 'help' to show help. More help for a command with 'help command'
                """, NAME, VERSION);
    }

    /**
     * Shutdown sokoshell: stop all tasks, executors and save
     * solutions if {@link #autoSaveSolution} is {@code true}.
     * Also print goodbye message
     */
    private void shutdown() {
        isShutdown = true;

        taskList.stopAll();
        executor.shutdown();
        scheduledExecutor.shutdown();

        if (notificationHandler != null) {
            notificationHandler.shutdown();
        }

        if (autoSaveSolution) {
            try {
                saveAllSolution();
            } catch (IOException | JsonException e) {
                e.printStackTrace();
                System.err.println("Failed to save solutions");
            }
        }

        if (promptEnabled) {
            System.out.println("Goodbye!");
        }
    }



    /**
     * Wait for the user to write the command and then execute it.
     *
     * @return false to stop
     */
    private boolean waitInputAndExecute() {
        boolean reading = false;
        try {
            reading = true;
            String line = reader.readLine(getPrompt());
            reading = false;

            cli.execute(line);
        } catch (EndOfFileException e) { // thrown when user types ctrl+D and by the built-in exit command
            return false;
        } catch (UserInterruptException e) {
            return !reading;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private String getPrompt() {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.styled(BOLD, NAME);

        if (selectedPack != null) {
            asb.append(" ");
            asb.styled(DEFAULT.foreground(GREEN + BRIGHT), selectedPack.name());

            int level = getSelectedLevelIndex();
            if (level >= 0) {
                asb.append(":");
                asb.styled(DEFAULT.foreground(GREEN + BRIGHT), String.valueOf(level + 1));
            }
        }

        asb.append("> ");

        return asb.toAnsi();
    }

    // STARTUP SCRIPT

    private boolean executeStartupScript(Path location) {
        StartupScript ss = new StartupScript(cli);
        try {
            ss.run(location);

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

            if (notificationHandler != null) {
                notificationHandler.clear();
            }
        }

        return SUCCESS;
    }

    private int gc(InputStream in, PrintStream out, PrintStream err) {
        Runtime.getRuntime().gc();

        return SUCCESS;
    }


    // =======================================================================
    // * Methods to retrieve information about sokoshell or modify sokoshell *
    // =======================================================================

    // solvers

    // actually private, but one day...
    private void addSolver(Solver solver) {
        solvers.put(solver.getName(), solver);
    }

    public Map<String, Solver> getSolvers() {
        return Collections.unmodifiableMap(solvers);
    }

    public Solver getSolver(String name) {
        return solvers.get(name);
    }

    // tasks
    public void addTask(SolverTask task) {
        taskList.offerTask(task);
    }

    public TaskList getTaskList() {
        return taskList;
    }


    public Path getStandardExportPath(Level level) {
        return getStandardExportPath(level.getPack(), level);
    }

    public Path getStandardExportPath(Pack pack, Level level) {
        if (pack == null && level == null) {
            return EXPORT_FOLDER.resolve("level");
        } else if (pack != null && level == null) {
            return EXPORT_FOLDER.resolve(pack.name());
        } else if (pack == null) {
            return EXPORT_FOLDER.resolve(String.valueOf((level.getIndex() + 1)));
        } else {
            return EXPORT_FOLDER.resolve(pack.name() + "_" + (level.getIndex() + 1));
        }
    }


    // packs

    /**
     * Add a pack if there is no pack with his name
     * @param pack the pack to add
     * @return true if the pack was added
     */
    public boolean addPack(Pack pack) {
        if (packs.containsKey(pack.name())) {
            return false;
        } else {
            packs.put(pack.name(), pack);

            return true;
        }
    }

    /**
     * Add a pack. If there were a pack with the same name, it is removed
     * @param pack the pack to add
     */
    public void addPackReplace(Pack pack) {
        packs.put(pack.name(), pack);
    }

    public Pack getPack(String name) {
        return packs.get(name);
    }

    public void addPackCandidates(List<Candidate> candidates) {
        for (Pack pack : getPacks()) {
            candidates.add(new Candidate(pack.name()));
        }
    }

    public void selectPack(Pack pack) {
        this.selectedPack = pack;
    }

    public Pack getSelectedPack() {
        return selectedPack;
    }

    public Collection<Pack> getPacks() {
        return packs.values();
    }

    // levels

    public void selectLevel(int index) {
        this.selectedLevel = index;
    }

    public Level getSelectedLevel() {
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.nLevels()) {
            return null;
        } else {
            return selectedPack.getLevel(selectedLevel);
        }
    }

    /**
     * @return -1 if there is no selected level otherwise the index of the selected level
     */
    public int getSelectedLevelIndex() {
        if (selectedPack == null || selectedLevel < 0 || selectedLevel >= selectedPack.nLevels()) {
            return -1;
        } else {
            return selectedLevel;
        }
    }

    // auto save

    public boolean isAutoSaveSolution() {
        return autoSaveSolution;
    }

    public void setAutoSaveSolution(boolean autoSaveSolution) {
        this.autoSaveSolution = autoSaveSolution;
    }

    public void saveAllSolution() throws IOException, JsonException {
        for (Pack pack : packs.values()) {
            pack.writeSolutions(null);
        }
    }

    // styles

    /**
     * Add the board style if there is no board style with his name
     * @param boardStyle the board style to add
     * @return true if it was added
     */
    public boolean addBoardStyle(BoardStyle boardStyle) {
        if (boardStyle == null) {
            return false;
        } else if (styles.containsKey(boardStyle.getName())) {
            return false;
        } else {
            styles.put(boardStyle.getName(), boardStyle);

            return true;
        }
    }

    /**
     * Add a board style. If there were a board style with the same name, it is removed
     * @param boardStyle the board style to add
     */
    public void addBoardStyleReplace(BoardStyle boardStyle) {
        if (boardStyle == null) {
            return;
        }

        styles.put(boardStyle.getName(), boardStyle);

        if (selectedStyle.getName().equals(boardStyle.getName())) {
            selectedStyle = boardStyle;
        }
    }

    public BoardStyle getBoardStyle(String name) {
        return styles.get(name);
    }

    public void addBoardStyleCandidates(List<Candidate> candidates) {
        for (BoardStyle boardStyle : getBoardStyles()) {
            candidates.add(new Candidate(boardStyle.getName()));
        }
    }

    public BoardStyle getBoardStyle() {
        return selectedStyle;
    }

    public void setBoardStyle(BoardStyle style) {
        this.selectedStyle = Objects.requireNonNull(style);
    }


    public Collection<BoardStyle> getBoardStyles() {
        return styles.values();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    public void newNotification(String message) {
        getNotificationHandler().newNotification(message);
    }

    public void newNotification(AttributedString message) {
        getNotificationHandler().newNotification(message);
    }

    public INotificationHandler getNotificationHandler() {
        if (notificationHandler == null) {
            if (promptEnabled) {
                notificationHandler = new NotificationHandler(reader);
            } else {
                notificationHandler = INotificationHandler.newFalseNotificationHandler();
            }
        }

        return notificationHandler;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public CommandLine getCli() {
        return cli;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public boolean isPromptEnabled() {
        return promptEnabled;
    }
}
