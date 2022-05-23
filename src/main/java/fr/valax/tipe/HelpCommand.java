package fr.valax.tipe;

import fr.valax.args.CommandLine;
import fr.valax.args.api.VaArgs;
import fr.valax.args.api.VoidCommand;

import java.util.Objects;

public class HelpCommand implements VoidCommand {

    private CommandLine cli;

    @VaArgs
    private String[] command;

    public HelpCommand() {
    }

    @Override
    public void run() {
        Objects.requireNonNull(cli);

        if (command != null && command.length > 0) {
            System.out.println(cli.getCommandHelp(String.join(" ", command)));
        } else {
            System.out.println(cli.getGeneralHelp());
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public boolean addHelp() {
        return false;
    }

    public void setCli(CommandLine cli) {
        this.cli = cli;
    }
}
