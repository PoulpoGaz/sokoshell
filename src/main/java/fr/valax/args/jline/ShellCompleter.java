package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.Token;
import fr.valax.args.Tokenizer;
import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.INode;
import fr.valax.sokoshell.utils.Utils;
import org.jline.builtins.Completers;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import javax.security.auth.login.AppConfigurationEntry;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShellCompleter implements Completer {

    protected final Completer fileNameCompleter = new Completers.FileNameCompleter();
    protected final CommandLine cli;

    protected INode<CommandDescriber> command;

    public ShellCompleter(CommandLine cli) {
        this.cli = cli;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String command = getCommand(line);

        boolean newToken = command.endsWith(" ");

        INode<CommandDescriber> cmd = cli.getCommands();
        boolean findingCommand = true;
        boolean endOfOptions =  false;

        List<Token> tokens = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer(command);
        while (tokenizer.hasNext()) {
            Token next = tokenizer.next();

            if (next.isWord() && findingCommand) {
                INode<CommandDescriber> child = findChild(cmd, next.value());

                if (child == null) {
                    findingCommand = false;
                } else {
                    cmd = child;
                }
            } else if (!next.isRedirect()) {
                findingCommand = false;

                if (next.isEndOfOption()) {
                    endOfOptions = true;
                }
            }

            tokens.add(next);
        }

        Token last = !tokens.isEmpty() ? tokens.get(tokens.size() - 1) : null;
        Token lastLast = tokens.size() >= 2 ? tokens.get(tokens.size() - 2) : null;
        Token lastLastLast = tokens.size() >= 3 ? tokens.get(tokens.size() - 3) : null;

        if (last != null) {

            if (last.isEndOfOption() || (endOfOptions && last.type() == Token.STD_ERR_IN_STD_OUT)) {
                completeVaArsg(reader, line, candidates, cmd.getValue());

            } else if ((isOption(last, true) && !endOfOptions) || last.type() == Token.STD_ERR_IN_STD_OUT) {
                completeOptions(candidates, cmd.getValue());

            } else if (last.isRedirect()) {
                completeRedirect(reader, line, candidates, last);

            } else if (last.isWord() && lastLast != null) {

                if (isOption(lastLast, false)) {
                    boolean completedOption = completeOption(reader, line, candidates, cmd.getValue(), last.value());

                    if (!completedOption) {
                        completeOptions(candidates, cmd.getValue());
                    }
                } else if (lastLast.isRedirect() && !newToken) {
                    completeRedirect(reader, line, candidates, lastLast);
                } else if (lastLast.isWord() && lastLastLast != null && isOption(lastLastLast, false) && !newToken) {
                    completeOption(reader, line, candidates, cmd.getValue(), lastLast.value());
                }
            }
        }

        if (candidates.isEmpty()) {
            CommandDescriber desc = cmd.getValue();

            if (desc != null && desc.nOptions() > 0) {
                candidates.add(new Candidate("-", "-", null, null, null, null, false));
            }
        }

        if (findingCommand) {
            for (INode<CommandDescriber> child : cmd.getChildren()) {
                candidates.add(new Candidate(child.getValue().getName()));
            }
        }
    }

    protected boolean isOption(Token token, boolean last) {
        if (last) {
            return token.value().equals("-") || token.value().equals("--");
        } else {
            return token.isOption();
        }
    }

    protected String getCommand(ParsedLine line) {
        char[] chars = line.line().toCharArray();

        int commandStart = 0;

        boolean escape = false;
        boolean inQuote = false;
        for (int i = 0; i < line.cursor(); i++) {
            char c = chars[i];

            boolean escape2 = escape;
            escape = false;

            if (escape2) {
                continue;

            } else if (c == '\\') {
                escape = true;

            } else if (c == '"') {
                inQuote = !inQuote;

            } else if (inQuote) {
                continue;

            } else if (c == '|' || c == ';') { // command separator
                commandStart = i + 1;
            }
        }

        return line.line()
                .substring(commandStart, line.cursor());
    }


    protected INode<CommandDescriber> findChild(INode<CommandDescriber> parent, String name) {
        for (INode<CommandDescriber> child : parent.getChildren()) {
            if (child.getValue().getName().equals(name)) {
                return child;
            }
        }

        return null;
    }

    protected void completeVaArsg(LineReader reader, ParsedLine line, List<Candidate> candidates, CommandDescriber desc) {
        Command command = desc.getCommand();

        if (command instanceof JLineCommand jLineCommand) {
            jLineCommand.completeVaArgs(reader, line, candidates);
        }
    }

    protected void completeOptions(List<Candidate> candidates, CommandDescriber desc) {
        OptionIterator iterator = desc.optionIterator();

        while (iterator.hasNext()) {
            OptionGroup group = iterator.currentGroup();

            Option opt = iterator.next();
            String firstName = opt.names()[0];
            String name = firstName.length() == 1 ? "-" + firstName : "--" + firstName;
            String groupName = group == null ? null : group.name();
            String description = ArgsUtils.first(opt.description());

            candidates.add(new Candidate(name, name, groupName, description, null, null, true));
        }
    }

    private void completeRedirect(LineReader reader, ParsedLine line, List<Candidate> candidates, Token last) {
        if (last.type() != Token.READ_INPUT_UNTIL) {
            fileNameCompleter.complete(reader, line, candidates);
        }
    }

    private boolean completeOption(LineReader reader, ParsedLine line, List<Candidate> candidates, CommandDescriber desc, String optionName) {
        Command cmd = desc.getCommand();

        if (cmd instanceof JLineCommand jLineCommand) {
            for (Option opt : desc) {
                if (ArgsUtils.contains(opt.names(), optionName)) {
                    jLineCommand.completeOption(reader, line, candidates, opt);
                    return true;
                }
            }
        }

        return false;
    }
}