package fr.valax.tipe;

import fr.valax.args.api.Command;

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
