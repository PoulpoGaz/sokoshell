package fr.valax.sokoshell;

public abstract class AbstractVoidCommand extends AbstractCommand<Void> {

    public static AbstractVoidCommand newCommand(Runnable runnable, String name, String usage) {
        return newCommand(runnable, name, usage, true);
    }

    public static AbstractVoidCommand newCommand(Runnable runnable, String name, String usage, boolean addHelp) {
        return new AbstractVoidCommand() {
            @Override
            public void run() {
                runnable.run();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getUsage() {
                return usage;
            }

            @Override
            public boolean addHelp() {
                return addHelp;
            }
        };
    }

    @Override
    protected Void executeImpl() {
        run();
        return null;
    }

    public abstract void run();
}
