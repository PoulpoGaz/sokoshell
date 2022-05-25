package fr.valax.sokoshell;

/**
 * @author PoulpoGaz
 */
public class ExitCommand extends AbstractCommand<Boolean> {

    public ExitCommand(SokoShellHelper helper) {
        super(helper);
    }

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
