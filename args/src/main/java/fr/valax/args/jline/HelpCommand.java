package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.api.VaArgs;

import java.io.InputStream;
import java.io.PrintStream;

public class HelpCommand implements JLineCommand {

    private CommandLine cli;

    @VaArgs
    private String[] args;

    @Override
    public int execute(InputStream in, PrintStream out, PrintStream err) {
        if (args.length == 0) {
            out.println(cli.getGeneralHelp());
        } else {
            String help = cli.getCommandHelp(String.join(" ", args));
            out.println(help);
        }

        return 0;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getShortDescription() {
        return "command help";
    }

    @Override
    public String[] getUsage() {
        return new String[] {"help [COMMAND]"};
    }

    public CommandLine getCli() {
        return cli;
    }

    public void setCli(CommandLine cli) {
        this.cli = cli;
    }
}
