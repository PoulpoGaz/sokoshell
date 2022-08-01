package fr.valax.sokoshell.commands.unix;

import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.sokoshell.commands.AbstractCommand;

import java.io.InputStream;
import java.io.PrintStream;

public class Echo extends AbstractCommand {

    @Option(names = "n", description = "do not output the trailing newline")
    private boolean trailingNewSpace;

    @VaArgs
    private String[] echo;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        for (int i = 0; i < echo.length; i++) {
            out.print(echo[i]);

            if (i + 1 < echo.length) {
                out.print(' ');
            }
        }

        if (!trailingNewSpace) {
            out.println();
        }

        return 0;
    }

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public String getShortDescription() {
        return "display a line of text";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
