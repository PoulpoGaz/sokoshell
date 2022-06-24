package fr.valax.args.api;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author PoulpoGaz
 */
public interface Command {

    int SUCCESS = 0;
    int FAILURE = 1;

    /**
     * @return the output of this command
     */
    int execute(InputStream in, PrintStream out, PrintStream err);

    /**
     * @return the name of the command
     */
    String getName();

    /**
     * @return A text to explains the user how to use the command.
     */
    String getUsage();

    /**
     * @return true to automatically add -h/--help option
     */
    boolean addHelp();
}
