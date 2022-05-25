package fr.valax.sokoshell;

import fr.valax.args.api.Command;

public abstract class AbstractCommand<T> implements Command<T> {

    protected final SokoShellHelper helper;

    public AbstractCommand(SokoShellHelper helper) {
        this.helper = helper;
    }
}
