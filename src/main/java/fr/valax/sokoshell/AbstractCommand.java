package fr.valax.sokoshell;

import fr.valax.args.repl.REPLCommand;

public abstract class AbstractCommand<T> implements REPLCommand<T> {

    protected final SokoShellHelper helper = SokoShellHelper.INSTANCE;

    @Override
    public T execute() {
        helper.lock();

        try {
            return executeImpl();
        } finally {
            helper.unlock();
        }
    }

    protected abstract T executeImpl();
}