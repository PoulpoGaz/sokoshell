package fr.valax.args.jline;

import fr.valax.args.CommandLine;
import fr.valax.args.Token;
import fr.valax.args.Tokenizer;
import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.INode;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShellCompleter implements Completer {

    protected final CommandLine cli;

    protected INode<CommandDescriber> command;

    public ShellCompleter(CommandLine cli) {
        this.cli = cli;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String command = getCommand(line);
        boolean newToken = command.endsWith(" ");

        BaseContext base = new BaseContext(cli.getCommandSpecs());

        try {
            Context context = base;
            Tokenizer tokenizer = new Tokenizer(command);
            tokenizer.disableAlias();

            Token last = null;
            while (tokenizer.hasNext()) {
                last = tokenizer.next();

                context = context.on(last, !tokenizer.hasNext(), newToken);
            }

            context.complete(last, reader, command, line, candidates, newToken);
        } finally {
            if (base.spec != null) {
                base.spec.reset();
            }
        }
    }

    protected String getCommand(ParsedLine line) {
        char[] chars = line.line().toCharArray();

        int commandStart = 0;

        boolean escape = false;
        boolean inQuote = false;
        for (int i = 0; i < line.cursor(); i++) {
            char c = chars[i];

            if (escape) {
                escape = false;
            } else if (c == '\\') {
                escape = true;

            } else if (c == '"') {
                inQuote = !inQuote;

            } else if (!inQuote && (c == '|' || c == ';')) { // command separator
                commandStart = i + 1;
            }
        }

        return line.line()
                .substring(commandStart, line.cursor());
    }

    private static abstract class Context {

        protected abstract Context on(Token token, boolean last, boolean newToken);

        protected abstract void complete(Token lastToken, LineReader reader, String commandString, ParsedLine line, List<Candidate> candidates, boolean newToken);

        protected void addAllOptions(boolean longName, boolean shortName,
                                     String optionNameStart, Set<CommandLine.OptionSpec> presentOptions,
                                     List<Candidate> candidates, CommandLine.CommandSpec desc) {
            if (!(longName | shortName)) {
                return;
            }


            for (CommandLine.OptionSpec opt : desc.getOptions().values()) {
                if (presentOptions.contains(opt) && !opt.allowDuplicate()) {
                    continue;
                }

                String name = null;
                if (longName) {
                    for (String n : opt.getLongNames()) {
                        if (n.startsWith(optionNameStart)) {
                            name = "--" + n;
                            break;
                        }
                    }
                }
                if (shortName && name == null) {
                    for (char c : opt.getShortNames()) {
                        if (optionNameStart.charAt(0) == c) {
                            name = "-" + c;
                            break;
                        }
                    }
                }

                if (name == null) {
                    continue;
                }

                OptionGroup group = opt.getGroup();
                String groupName = group == null ? null : group.name();
                String description = ArgsUtils.first(opt.getOption().description());

                candidates.add(new Candidate(name, name, groupName, description, null, null, true));
            }
        }

        protected boolean isOption(Token token, boolean last, boolean newToken) {
            if (last && !newToken) {
                return token.value().equals("-") || token.value().equals("--");
            } else {
                return token.isOption();
            }
        }
    }

    private static class BaseContext extends Context {

        private boolean findingCommand = true;
        private boolean endOfOptions = false;
        private final Set<CommandLine.OptionSpec> presentOptions = new HashSet<>();

        private INode<CommandLine.CommandSpec> command;
        private CommandLine.CommandSpec spec;

        public BaseContext(INode<CommandLine.CommandSpec> rootCommand) {
            this.command = rootCommand;
        }

        @Override
        protected Context on(Token token, boolean last, boolean newToken) {
            if (isOption(token, last, newToken) && !endOfOptions) {
                findingCommand = false;
                return new OptionContext(this, command.getValue(), token.value());
            } else if (token.isEndOfOption()) {
                endOfOptions = true;
                findingCommand = false;
            } else if (token.isRedirect()) {
                if (token.type() == Token.STD_ERR_IN_STD_OUT) {
                    return this;
                }

                return new RedirectContext(this, token);
            } else if (token.isWord() && findingCommand) {
                INode<CommandLine.CommandSpec> child = findChild(command, token.value());

                if (child == null) {
                    if (spec != null) {
                        addVaArgs(token.value());
                        findingCommand = false;
                    }
                } else {
                    command = child;
                    spec = command.getValue();
                }
            } else if (token.isWord()) {
                addVaArgs(token.value());
            }

            return this;
        }

        protected void addVaArgs(String arg) {
            if (spec.getVaargs() != null) {
                spec.getVaargs().addValue(arg);
            }
        }

        @Override
        protected void complete(Token last, LineReader reader, String commandString, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (findingCommand ||
                    (last != null && !isOption(last, true, newToken))) {
                for (INode<CommandLine.CommandSpec> child : command.getChildren()) {
                    candidates.add(new Candidate(child.getValue().getName()));
                }
            }

            CommandLine.CommandSpec desc = command.getValue();

            if (desc != null) {
                if (desc.getOptions().size() != presentOptions.size() && !endOfOptions && newToken) {
                    candidates.add(new Candidate("-", "-", null, null, null, null, false));
                }

                if (last != null && last.isWord() || newToken) {
                    Command command = desc.getCommand();

                    if (command instanceof JLineCommand jLineCommand) {
                        if (newToken) {
                            jLineCommand.complete(reader, commandString, spec, candidates, null, null);
                        } else {
                            jLineCommand.complete(reader, commandString, spec, candidates, null, last.value());
                        }
                    }
                }
            }
        }

        protected INode<CommandLine.CommandSpec> findChild(INode<CommandLine.CommandSpec> parent, String name) {
            for (INode<CommandLine.CommandSpec> child : parent.getChildren()) {
                if (child.getValue().getName().equals(name)) {
                    return child;
                }
            }

            return null;
        }

        public void addOption(CommandLine.OptionSpec option) {
            if (option != null) {
                presentOptions.add(option);
            }
        }

        public Set<CommandLine.OptionSpec> getPresentOptions() {
            return presentOptions;
        }
    }

    protected static class OptionContext extends Context {

        private final BaseContext baseContext;
        private final CommandLine.CommandSpec command;
        private final String optionStart;

        private CommandLine.OptionSpec option;
        private boolean optionNameParsing = false;

        public OptionContext(BaseContext baseContext, CommandLine.CommandSpec command, String optionStart) {
            this.baseContext = baseContext;
            this.command = command;
            this.optionStart = optionStart;
        }

        @Override
        protected Context on(Token token, boolean last, boolean newToken) {
            if (token.isWord()) {

                if (option == null) {
                    optionNameParsing = true;
                    option = command.findOption(token.value());

                    if (option != null) {
                        option.markPresent();
                    }

                    if (option == null || !option.hasArgument()) {
                        // cursor is just after the token
                        // so OptionContext must complete
                        if (last && !newToken) {
                            return this;
                        } else {
                            baseContext.addOption(option);

                            return baseContext;
                        }
                    }

                    return this;
                } else {
                    option.addArgument(token.value());
                    optionNameParsing = false;

                    if (last && !newToken) {
                        return this;
                    } else {
                        baseContext.addOption(option);

                        return baseContext;
                    }
                }
            } else {
                return baseContext.on(token, last, newToken);
            }
        }

        @Override
        protected void complete(Token last, LineReader reader, String commandString, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (option == null || (optionNameParsing && !newToken)) {
                if (isOption(last, true, newToken)) {
                    addAllOptions(true, true, "", baseContext.getPresentOptions(), candidates, command);
                } else {
                    addAllOptions(optionStart.equals("--"), optionStart.equals("-"),
                            last.value(), baseContext.getPresentOptions(), candidates, command);
                }
            } else {
                completeOption(reader, commandString, last.value(), candidates);
            }
        }

        private void completeOption(LineReader reader, String commandString, String arg, List<Candidate> candidates) {
            if (command.getCommand() instanceof JLineCommand jLineCommand) {
                jLineCommand.complete(reader, commandString, baseContext.spec, candidates, option, arg);
            }
        }
    }

    protected static class RedirectContext extends Context {

        private final BaseContext baseContext;
        private final Token redirect;

        public RedirectContext(BaseContext baseContext, Token redirect) {
            this.baseContext = baseContext;
            this.redirect = redirect;
        }

        @Override
        protected Context on(Token token, boolean last, boolean newToken) {
            if (token.isWord() && last && !newToken) {
                return this;
            } else if (token.isWord()) {
                return baseContext;
            } else {
                return baseContext.on(token, last, newToken);
            }
        }

        @Override
        protected void complete(Token last, LineReader reader, String commandString, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (redirect.type() != Token.READ_INPUT_UNTIL) {
                FileNameCompleter.INSTANCE.complete(reader, last.value(), candidates);
            }
        }
    }
}