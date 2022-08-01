package fr.valax.sokoshell.commands.select;

import fr.valax.sokoshell.commands.AbstractCommand;

import java.io.InputStream;
import java.io.PrintStream;

public class Select extends AbstractCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        err.println("Use 'select pack PACK' to select a pack");
        err.println("Use 'select level INDEX' to select a level");
        return FAILURE;
    }

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public String getShortDescription() {
        return "select";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}