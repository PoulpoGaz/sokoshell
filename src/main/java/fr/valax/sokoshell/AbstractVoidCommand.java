package fr.valax.sokoshell;

public abstract class AbstractVoidCommand extends AbstractCommand<Void> {

    @Override
    protected Void executeImpl() {
        run();
        return null;
    }

    public abstract void run();
}
