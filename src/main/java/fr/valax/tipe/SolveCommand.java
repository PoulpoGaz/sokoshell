package fr.valax.tipe;

import fr.valax.args.api.VoidCommand;

public class SolveCommand implements VoidCommand {

    @Override
    public void run() {
        System.out.println("Solved!");
    }

    @Override
    public String getName() {
        return "solve";
    }

    @Override
    public String getUsage() {
        return "solve a sokoban";
    }

    @Override
    public boolean addHelp() {
        return true;
    }
}
