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

        Context context = new BaseContext(cli.getCommands());
        Tokenizer tokenizer = new Tokenizer(command);
        tokenizer.disableAlias();

        Token last = null;
        while (tokenizer.hasNext()) {
            last = tokenizer.next();

            context = context.on(last, !tokenizer.hasNext(), newToken);
        }

        context.complete(last, reader, line, candidates, newToken);
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

    private static abstract class Context {

        protected abstract Context on(Token token, boolean last, boolean newToken);

        protected abstract void complete(Token lastToken, LineReader reader, ParsedLine line, List<Candidate> candidates, boolean newToken);

        protected void addAllOptions(boolean longName, boolean shortName,
                                     String optionNameStart, Set<Option> presentOptions, List<Candidate> candidates, CommandDescriber desc) {
            if (!(longName | shortName)) {
                return;
            }

            OptionIterator iterator = desc.optionIterator();

            while (iterator.hasNext()) {
                OptionGroup group = iterator.currentGroup();

                Option opt = iterator.next();

                if (presentOptions.contains(opt) && !opt.allowDuplicate()) {
                    continue;
                }

                String name = null;
                for (String n : opt.names()) {
                    if (longName != shortName) {
                        if (longName && n.length() <= 1) {
                            continue;
                        }
                        if (shortName && n.length() > 1) {
                            continue;
                        }
                    }

                    if (n.startsWith(optionNameStart)) {
                        if (n.length() > 1) {
                            name = "--" + n;
                        } else {
                            name = "-" + n;
                        }
                    }
                }

                if (name == null) {
                    continue;
                }

                String groupName = group == null ? null : group.name();
                String description = ArgsUtils.first(opt.description());

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
        private final Set<Option> presentOptions = new HashSet<>();

        private INode<CommandDescriber> command;

        public BaseContext(INode<CommandDescriber> rootCommand) {
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
                INode<CommandDescriber> child = findChild(command, token.value());

                if (child == null) {
                    findingCommand = false;
                } else {
                    command = child;
                }
            }

            return this;
        }

        @Override
        protected void complete(Token last, LineReader reader, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (findingCommand ||
                    (last != null && !isOption(last, true, newToken))) {
                for (INode<CommandDescriber> child : command.getChildren()) {
                    candidates.add(new Candidate(child.getValue().getName()));
                }
            }

            CommandDescriber desc = command.getValue();

            if (desc != null) {
                if (desc.nOptions() != presentOptions.size() && !endOfOptions) {
                    candidates.add(new Candidate("-", "-", null, null, null, null, false));
                }

                if (last != null && last.isWord() || newToken) {
                    Command command = desc.getCommand();

                    if (command instanceof JLineCommand jLineCommand) {
                        if (newToken) {
                            jLineCommand.completeVaArgs(reader, "", candidates);
                        } else {
                            jLineCommand.completeVaArgs(reader, last.value(), candidates);
                        }
                    }
                }
            }
        }

        protected INode<CommandDescriber> findChild(INode<CommandDescriber> parent, String name) {
            for (INode<CommandDescriber> child : parent.getChildren()) {
                if (child.getValue().getName().equals(name)) {
                    return child;
                }
            }

            return null;
        }

        public void addOption(Option option) {
            if (option != null) {
                presentOptions.add(option);
            }
        }

        public Set<Option> getPresentOptions() {
            return presentOptions;
        }
    }

    protected static class OptionContext extends Context {

        private final BaseContext baseContext;
        private final CommandDescriber command;
        private final String optionStart;

        private Option option;
        private boolean optionNameParsing = false;

        public OptionContext(BaseContext baseContext, CommandDescriber command, String optionStart) {
            this.baseContext = baseContext;
            this.command = command;
            this.optionStart = optionStart;
        }

        @Override
        protected Context on(Token token, boolean last, boolean newToken) {
            if (token.isWord()) {

                if (option == null) {
                    optionNameParsing = true;
                    for (Option opt : command) {
                        if (contains(opt, token.value())) {
                            option = opt;
                        }
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

                } else {
                    optionNameParsing = false;

                    if (last && !newToken) {
                        return this;
                    } else {
                        baseContext.addOption(option);

                        return baseContext;
                    }
                }
            } else {
                throw new IllegalStateException();
            }

            return this;
        }

        private boolean contains(Option opt, String name) {
            for (String n : opt.names()) {
                if (optionStart.length() >= 2) {
                    if (n.length() > 1 && n.equals(name)) {
                        return true;
                    }
                } else if (n.equals(name)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        protected void complete(Token last, LineReader reader, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (option == null || (optionNameParsing && !newToken)) {
                if (isOption(last, true, newToken)) {
                    addAllOptions(true, true, "", baseContext.getPresentOptions(), candidates, command);
                } else {
                    addAllOptions(optionStart.equals("--"), optionStart.equals("-"),
                            last.value(), baseContext.getPresentOptions(), candidates, command);
                }
            } else {
                completeOption(reader, last.value(), candidates);
            }
        }

        private void completeOption(LineReader reader, String arg, List<Candidate> candidates) {
            if (command.getCommand() instanceof JLineCommand jLineCommand) {
                jLineCommand.completeOption(reader, arg, candidates, option);
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
            } else {
                return baseContext;
            }
        }

        @Override
        protected void complete(Token last, LineReader reader, ParsedLine line, List<Candidate> candidates, boolean newToken) {
            if (redirect.type() != Token.READ_INPUT_UNTIL) {
                FileNameCompleter.INSTANCE.complete(reader, last.value(), candidates);
            }
        }
    }
}