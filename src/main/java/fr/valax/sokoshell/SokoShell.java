package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.ParseException;
import fr.valax.args.utils.TypeException;

import java.util.Scanner;

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
        if (args.length > 0) {
            if (sokoshell.execute(args)) {
                return;
            }
        }

        try {
            sokoshell.loop();
        } finally {
            sokoshell.goodbye();
        }

        /*try {
            Pack pack = PackReaders.read(Path.of("levels/Microba0.8xv"));
            Solver dfs = BasicBrutalSolver.newDFSSolver();

            Level level = pack.levels().get(0);

            PrintCommand.printMap(level);
            List<State> solution = new ArrayList<>();

            System.out.println(dfs.solve(level, solution));
            System.out.println(solution);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
    }

    private final CommandLine cli;
    private final SokoShellHelper helper;

    private SokoShell() throws CommandLineException {
        helper = new SokoShellHelper();

        HelpCommand help = new HelpCommand();

        cli = new CommandLineBuilder()
                .addDefaultConverters()
                .addCommand(new SolveCommand(helper))
                .addCommand(new PrintCommand(helper))
                .addCommand(new LoadCommand(helper))
                .addCommand(new ExitCommand(helper))
                .addCommand(new ListCommand(helper))
                .addCommand(help)
                .build();

        help.setCli(cli);
    }

    private void welcome() {
        System.out.printf("""
                Welcome to sokoshell - Version %s
                Type 'help' to show help
                """, VERSION);
    }

    private void goodbye() {
        System.out.println("Goodbye!");
    }

    private void loop() {
        Scanner sc = new Scanner(System.in);

        boolean exit = false;
        System.out.print("sokoshell> ");
        while (!exit) {
            if (sc.hasNextLine()) {
                String[] args = ArgsUtils.splitQuoted(sc.nextLine());

                exit = execute(args);

                if (!exit) {
                    System.out.print("sokoshell> ");
                }
            } else {
                Thread.onSpinWait();
            }
        }
    }

    /**
     * @param args the command to execute
     * @return true if a command require to the program to quit
     * @throws IllegalStateException if a CommandLineException which isn't a
     * ParseException or a TypeException is thrown
     */
    private boolean execute(String[] args) {
        try {
            Object object = cli.execute(args);

            if (object instanceof Boolean b) {
                return b;
            }

            return false;
        } catch (ParseException | TypeException e) {
            System.out.println(e.getMessage());
            return false;
        }  catch (CommandLineException e) {
            throw new IllegalStateException(e);
        }
    }
}
