package fr.valax.sokoshell.commands.pack;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class SaveCommand extends PackCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        Pack pack = getPack(this.pack);

        try {
            pack.writeSolutions(null);
        } catch (IOException | JsonException e) {
            e.printStackTrace(err);
            err.println("Failed to save solutions");

            return FAILURE;
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getShortDescription() {
        return "Save all solutions of a pack";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }
}
