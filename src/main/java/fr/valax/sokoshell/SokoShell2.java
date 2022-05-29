package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.repl.REPLCommand;
import fr.valax.args.repl.REPLCommandRegistry;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.AutosuggestionWidgets;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SokoShell2 {

    private static Command<?> createDumbCommand(String name) {
        return new REPLCommand<>() {

            @Override
            public void completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, Option option) {
                if (ArgsUtils.contains(option.names(), "opt")) {
                    candidates.add(new Candidate("0"));
                    candidates.add(new Candidate("1"));
                    candidates.add(new Candidate("2"));
                    candidates.add(new Candidate("42"));
                    candidates.add(new Candidate("69"));
                }
            }

            @Option(names = "opt", hasArgument = true)
            private int opt;

            @Option(names = "blob")
            private boolean blob;

            @Override
            public Object execute() {
                System.out.printf("%s - opt= %d%n", name, opt);

                if (blob) {
                    System.out.println("blob");
                }
                return null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getUsage() {
                return "Dumb command usage: " + name;
            }

            @Override
            public boolean addHelp() {
                return false;
            }
        };
    }

    public static void main(String[] args) throws CommandLineException {

        SokoShellHelper helper = new SokoShellHelper();
        CommandLine cli = new CommandLineBuilder()
                .addDefaultConverters()
                .subCommand(createDumbCommand("hello"))
                    .subCommand(createDumbCommand("world"))
                        .addCommand(createDumbCommand("41"))
                        .addCommand(createDumbCommand("42"))
                        .addCommand(createDumbCommand("43"))
                        .endSubCommand()
                    .addCommand(createDumbCommand("world2"))
                    .addCommand(createDumbCommand("world3"))
                    .addCommand(createDumbCommand("you"))
                    .addCommand(createDumbCommand("peter"))
                    .endSubCommand()
                .addCommand(new SolveCommand(helper))
                .addCommand(new PrintCommand(helper))
                .addCommand(new LoadCommand(helper))
                .addCommand(new ListCommand(helper))
                .build();

        Parser parser = new DefaultParser();
        REPLCommandRegistry shellRegistry = new REPLCommandRegistry(cli);

        try (Terminal terminal = TerminalBuilder.terminal()) {
            SystemRegistry registry = new SystemRegistryImpl(parser, terminal, () -> Path.of(""), null);
            registry.setCommandRegistries(shellRegistry);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .appName("sokoshell")
                    .history(new DefaultHistory())
                    .highlighter(new DefaultHighlighter())
                    .parser(parser)
                    .completer(shellRegistry.compileCompleters())
                    .variable(LineReader.HISTORY_FILE, System.getProperty("user.home") + "/.sokoshell_history")  // won't work on windows!!!!
                    .build();

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            while (true) {
                registry.cleanUp();
                String line = reader.readLine("sokoshell> ");

                try {
                    registry.execute(line);
                } catch (EndOfFileException e) { // thrown when user types ctrl+D and by the built-in exit command
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
