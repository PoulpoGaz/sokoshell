package fr.valax.sokoshell;

import fr.valax.args.api.VoidCommand;

public class PrintCommand implements VoidCommand {

    @Override
    public void run() {
        System.out.println("Print!");
    }

    @Override
    public String getName() {
        return "print";
    }

    @Override
    public String getUsage() {
        return "print a state";
    }

    @Override
    public boolean addHelp() {
        return true;
    }
}
