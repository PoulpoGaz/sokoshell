package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.ParseException;
import fr.valax.args.utils.TypeException;

import java.util.*;

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
                String[] args = splitQuoted(sc.nextLine());

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

    private String[] splitQuoted(String line) {
        List<String> split = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        boolean inQuotation = false;
        boolean escapeNext = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            boolean escape = escapeNext;
            escapeNext = false;

            if (escape) {
                builder.append(c);

            } else if (c == '\\') {
                escapeNext = true;

            } else if (c == '"') {
                inQuotation = !inQuotation;

            } else if (c == ' ' && !inQuotation) {
                if (!builder.isEmpty()) {
                    split.add(builder.toString());
                    builder.setLength(0);
                }

            } else {
                builder.append(c);
            }

        }

        if (!builder.isEmpty()) {
            split.add(builder.toString());
        }

        return split.toArray(new String[0]);
    }
}
