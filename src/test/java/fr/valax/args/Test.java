package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.ParseException;
import org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Test {

    @org.junit.jupiter.api.Test
    void test() {
        CommandLine cli;
        try {
            cli = new CommandLineBuilder()
                    .addCommand(new Cmd("print"))
                    .subCommand(new Cmd("init"))
                        .addCommand(new Cmd("proj"))
                        .addCommand(new Cmd("debug"))
                        .endSubCommand()
                    .addCommand(new Cmd("list"))
                    .build();
        } catch (CommandLineException e) {
            throw new RuntimeException(e);
        }

        try {
            cli.parse(new String[] {"prit", "-"});
            System.out.println("---------------");
            cli.parse(new String[] {"print", "-"});
            System.out.println("---------------");
            cli.parse(new String[] {"print", "-h"});
        } catch (CommandLineException e) {
            throw new RuntimeException(e);
        }

        //cli.setShowHelp(false);
        //assertThrows(ParseException.class, () ->  cli.parse(new String[] {}));
        //assertDoesNotThrow(() -> cli.parse(new String[] {"print", "-h"}));
        //assertDoesNotThrow(() -> cli.parse(new String[] {"init", "proj"}));
        //assertDoesNotThrow(() -> cli.parse(new String[] {"init", "debug"}));
        //assertDoesNotThrow(() -> cli.parse(new String[] {"list"}));
    }

    private static class Cmd implements Command<Object> {

        private final String name;

        @Option(names = "a", description = "The super cool a optional option")
        private boolean a;

        @Option(names = "b", argName = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", description = "But b is better, it is super giga mega cool!")
        private String b;

        @Option(names = "c", description =
                "However, c is god. It is super powerful. He can speed up your productivity by 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679821480865132823066470938446095505822317253594081284811 percent!!")
        private boolean c;

        public Cmd(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getUsage() {
            return (name + " ").repeat(20);
        }

        @Override
        public boolean addHelp() {
            return true;
        }

        @Override
        public Object execute() {
            System.out.println(name);
            return null;
        }
    }
}
