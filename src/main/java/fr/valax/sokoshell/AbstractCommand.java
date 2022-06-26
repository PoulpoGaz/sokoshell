package fr.valax.sokoshell;

import fr.valax.args.jline.JLineCommand;
import fr.valax.sokoshell.utils.TriFunction;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

public abstract class AbstractCommand implements JLineCommand {

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage) {
        return newCommand(function, name, usage, true);
    }

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage,
                                             boolean addHelp) {
        if (usage == null) {
            return newCommand(function, name, null, new String[0], addHelp);
        } else {
            String[] parts = usage.split("[\n\r]");

            if (parts.length == 1) {
                return newCommand(function, name, parts[0], new String[0], addHelp);
            } else {
                return newCommand(function, name, parts[0], Arrays.copyOfRange(parts, 1, parts.length), addHelp);
            }
        }
    }

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String shortDescription,
                                             String[] usage,
                                             boolean addHelp) {
        return new AbstractCommand() {
            @Override
            protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
                return function.apply(in, out, err);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getShortDescription() {
                return shortDescription;
            }

            @Override
            public String[] getUsage() {
                return usage;
            }

            @Override
            public boolean addHelp() {
                return addHelp;
            }
        };
    }


    protected final SokoShellHelper helper = SokoShellHelper.INSTANCE;

    @Override
    public int execute(InputStream in, PrintStream out, PrintStream err) {
        helper.lock();

        try {
            return executeImpl(in, out, err);
        } finally {
            helper.unlock();
        }
    }

    protected abstract int executeImpl(InputStream in, PrintStream out, PrintStream err);
}
