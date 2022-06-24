package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class SaveCommand extends PackCommand {

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        Pack pack = getPack();

        if (pack == null) {
            return FAILURE;
        }

        try {
            pack.writeSolutions(null);
        } catch (IOException | JsonException e) {
            e.printStackTrace();
            System.out.println("Failed to save solutions");

            return FAILURE;
        }

        return SUCCESS;
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public String getUsage() {
        return "Save all solutions of a pack";
    }
}
