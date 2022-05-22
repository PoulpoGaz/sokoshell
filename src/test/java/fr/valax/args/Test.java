package fr.valax.args;

import fr.valax.args.api.Command;

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

        assertThrows(ParseException.class, () ->  cli.parse(new String[] {}));
        assertDoesNotThrow(() -> cli.parse(new String[] {"print"}));
        assertDoesNotThrow(() -> cli.parse(new String[] {"init", "proj"}));
        assertDoesNotThrow(() -> cli.parse(new String[] {"init", "debug"}));
        assertDoesNotThrow(() -> cli.parse(new String[] {"list"}));
    }

    private static class Cmd implements Command<Object> {

        private final String name;

        public Cmd(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public boolean help() {
            return false;
        }

        @Override
        public Object execute() {
            System.out.println(name);
            return null;
        }
    }
}
