package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.CommandLineBuilder;
import fr.valax.args.ShellCommandRegistry;
import fr.valax.args.api.Command;
import fr.valax.args.utils.CommandLineException;
import org.jline.console.SystemRegistry;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.AutosuggestionWidgets;
import org.jline.widget.TailTipWidgets;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Path;

public class SokoShell2 {

    private static Command<?> createDumbCommand(String name) {
        return new Command<>() {
            @Override
            public Object execute() {
                System.out.println(name);
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
                    .addCommand(createDumbCommand("world"))
                    .addCommand(createDumbCommand("you"))
                    .addCommand(createDumbCommand("peter"))
                    .endSubCommand()
                .addCommand(new SolveCommand(helper))
                .addCommand(new PrintCommand(helper))
                .addCommand(new LoadCommand(helper))
                .addCommand(new ListCommand(helper))
                .build();

        Parser parser = new DefaultParser();
        ShellCommandRegistry shellRegistry = new ShellCommandRegistry(cli);

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
                    .build();

            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();

            TailTipWidgets tailTipWidgets = new TailTipWidgets(reader, registry::commandDescription, 5, TailTipWidgets.TipType.COMPLETER);
            tailTipWidgets.enable();

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
