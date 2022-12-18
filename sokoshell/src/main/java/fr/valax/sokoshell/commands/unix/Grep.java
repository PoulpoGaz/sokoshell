package fr.valax.sokoshell.commands.unix;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.sokoshell.commands.AbstractCommand;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Grep extends AbstractCommand {

    @Option(names = {"e", "regex", "pattern"}, hasArgument = true)
    private String pattern;

    @Option(names = {"i", "ignore-case"},
            description = "Ignore case distinctions in patterns and input data, so that characters that differ only in case match each other.")
    private boolean ignoreCase;

    @Option(names = "no-ignore-case",
            description = "Do not ignore case distinctions in patterns and input data. This is the default. This option is " +
                    "useful for passing to shell scripts that already use -i, to cancel its effects because the two " +
                    "options override each other.")
    private boolean noIgnoredCase;

    @Option(names = {"v", "invert-match"}, description = "Invert the sense of matching, to select non-matching lines.")
    private boolean invertMatch;

    @Option(names = {"x", "line-regexp"},
            description = "Select only those matches that exactly match the whole line. For a regular expression pattern, " +
                    "this is like parenthesizing the pattern and then surrounding it with ^ and $.")
    private boolean lineRegexp;

    @Option(names = {"c", "count"},
            description = "Suppress normal output; instead print a count of matching lines for each input file. With the -v, " +
                    "--invert-match option (see below), count non-matching lines.")
    private boolean count;

    @Option(names = {"color", "colour"})
    private boolean color;

    @VaArgs
    private String[] vaArgs;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (pattern == null && vaArgs.length == 0) {
            for (String u : getUsage()) {
                out.println(u);
            }

            return FAILURE;
        } else {
            int vaArgsStart = 0;

            String p;
            if (pattern == null) {
                p = vaArgs[0];
                vaArgsStart++;
            } else {
                p = this.pattern;
            }

            Pattern pattern;
            try {
                if (ignoreCase && !noIgnoredCase) {
                    pattern = Pattern.compile(p, Pattern.CASE_INSENSITIVE);
                } else {
                    pattern = Pattern.compile(p);
                }
            } catch (PatternSyntaxException e) {
                e.printStackTrace(err);
                err.println("Invalid pattern");
                return FAILURE;
            }

            if (vaArgs.length - vaArgsStart == 0) {
                try {
                    process(pattern, new BufferedReader(new InputStreamReader(in)), out);
                } catch (IOException e) {
                    e.printStackTrace(err);
                }

            } else {
                for (int i = vaArgsStart; i < vaArgs.length; i++) {
                    String file = vaArgs[i];

                    try {
                        if (file.equals("-")) {
                            process(pattern, new BufferedReader(new InputStreamReader(in)), out);
                        } else {
                            try (BufferedReader br = Files.newBufferedReader(Path.of(file))) {
                                process(pattern, br, out);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace(err);
                    }
                }
            }

            return SUCCESS;
        }
    }

    private void process(Pattern pattern, BufferedReader br, PrintStream out) throws IOException {
        int nMatch = 0;
        String line;
        while ((line = br.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);

            boolean match;
            if (lineRegexp) {
                match = matcher.matches();
            } else {
                match = matcher.find();
            }

            if (match == !invertMatch) {
                nMatch++;

                if (!count) {
                    if (invertMatch || !color) {
                        out.println(line);
                    } else {
                        out.println(color(line, matcher));
                    }
                }
            }
        }

        if (count) {
            out.println(nMatch);
        }
    }

    private String color(String line, Matcher matcher) {
        AttributedStringBuilder asb = new AttributedStringBuilder();

        matcher.reset();
        Iterator<MatchResult> results = matcher.results().iterator();

        int lastEnd = 0;
        while (results.hasNext()) {
            MatchResult current = results.next();

            if (lastEnd != current.start()) {
                asb.append(line, lastEnd, current.start());
            }
            asb.styled(AttributedStyle.BOLD.foreground(AttributedStyle.BLUE), line.substring(current.start(), current.end()));

            lastEnd = current.end();
        }

        if (lastEnd != line.length()) {
            asb.append(line, lastEnd, line.length());
        }

        return asb.toAnsi();
    }

    @Override
    public String getName() {
        return "grep";
    }

    @Override
    public String getShortDescription() {
        return "print lines that match patterns";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Usage: grep [OPTION]... PATTERNS [FILE]...",
                "Try 'grep --help' for more information."
        };
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command, List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option == null) {
            FileNameCompleter.INSTANCE.complete(reader, Objects.requireNonNullElse(argument, ""), candidates);
        }
    }
}