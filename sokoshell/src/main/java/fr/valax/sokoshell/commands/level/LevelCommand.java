package fr.valax.sokoshell.commands.level;

import fr.valax.args.api.Option;
import fr.valax.sokoshell.commands.pack.PackCommand;

public abstract class LevelCommand extends PackCommand {

    @Option(names = {"l", "level"}, hasArgument = true, argName = "Level index")
    protected Integer level;
}
