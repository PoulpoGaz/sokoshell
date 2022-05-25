package fr.valax.sokoshell;

import fr.valax.args.api.VoidCommand;

/**
 * @author PoulpoGaz
 */
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
