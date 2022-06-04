package fr.valax.sokoshell;

import fr.valax.sokoshell.solver.Pack;

public class ListCommand extends AbstractVoidCommand {

    @Override
    public void run() {
        for (Pack pack : helper.getPacks()) {
            System.out.println(pack.name());
        }
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "List all pack";
    }
}
