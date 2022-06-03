package fr.valax.sokoshell;

import fr.valax.args.repl.REPLCommand;

public abstract class AbstractCommand<T> implements REPLCommand<T> {

    protected final SokoShellHelper helper;

    public AbstractCommand(SokoShellHelper helper) {
        this.helper = helper;
    }
}
