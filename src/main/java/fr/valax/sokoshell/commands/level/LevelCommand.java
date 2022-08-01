package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.commands.PackCommand;

public abstract class LevelCommand extends PackCommand {

    @Option(names = {"i", "index"}, hasArgument = true, argName = "Level index")
    protected Integer index;
}
