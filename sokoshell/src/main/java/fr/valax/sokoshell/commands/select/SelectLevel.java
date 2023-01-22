package fr.valax.sokoshell.commands.select;

import fr.valax.args.api.VaArgs;
import fr.valax.sokoshell.commands.AbstractCommand;

import java.io.InputStream;
import java.io.PrintStream;

public class SelectLevel extends AbstractCommand {

    @VaArgs
    private int[] level;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (level.length == 0) {
            sokoshell().selectLevel(-1);
        } else {
            sokoshell().selectLevel(level[0] - 1);
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "level";
    }

    @Override
    public String getShortDescription() {
        return "select a level";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}