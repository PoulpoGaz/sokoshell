package fr.valax.sokoshell;

import fr.valax.args.repl.REPLCommand;
import fr.valax.sokoshell.utils.TriFunction;

import java.io.InputStream;
import java.io.PrintStream;

public abstract class AbstractCommand implements REPLCommand {

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage) {
        return newCommand(function, name, usage, true);
    }

    public static AbstractCommand newCommand(TriFunction<InputStream, PrintStream, PrintStream, Integer> function,
                                             String name,
                                             String usage,
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
            public String getUsage() {
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
