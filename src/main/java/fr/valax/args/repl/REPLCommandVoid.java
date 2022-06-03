package fr.valax.args.repl;

public interface REPLCommandVoid extends REPLCommand<Void> {

    @Override
    default Void execute() {
        run();
        return null;
    }

    void run();
}
