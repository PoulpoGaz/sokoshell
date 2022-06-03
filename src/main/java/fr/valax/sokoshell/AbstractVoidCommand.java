package fr.valax.sokoshell;

public abstract class AbstractVoidCommand extends AbstractCommand<Void> {

    public AbstractVoidCommand(SokoShellHelper helper) {
        super(helper);
    }

    @Override
    protected Void executeImpl() {
        run();
        return null;
    }

    public abstract void run();
}
