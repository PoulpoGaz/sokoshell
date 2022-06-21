package fr.valax.sokoshell;

import fr.poulpogaz.json.JsonException;
import fr.valax.sokoshell.solver.Pack;

import java.io.IOException;

public class SaveCommand extends PackCommand {

    @Override
    public void run() {
        Pack pack = getPack();

        if (pack == null) {
            return;
        }

        try {
            pack.writeSolutions(null);
        } catch (IOException | JsonException e) {
            e.printStackTrace();
            System.out.println("Failed to save solutions");
        }
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
