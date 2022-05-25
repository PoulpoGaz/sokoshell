package fr.valax.sokoshell;

import fr.valax.args.api.Command;

/**
 * @author PoulpoGaz
 */
public class ExitCommand implements Command<Boolean> {

    @Override
    public Boolean execute() {
        return true;
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getUsage() {
        return "exit the program";
    }

    @Override
    public boolean addHelp() {
        return true;
    }
}
