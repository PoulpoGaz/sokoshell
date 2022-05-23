package fr.valax.args.api;

public interface VoidCommand extends Runnable, Command<Void> {

    @Override
    default Void execute() {
        run();
        return null;
    }
}
