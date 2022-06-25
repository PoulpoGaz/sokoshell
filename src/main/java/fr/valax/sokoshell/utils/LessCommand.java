package fr.valax.sokoshell.utils;

import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.sokoshell.AbstractCommand;
import fr.valax.sokoshell.SokoShellHelper;
import org.jline.builtins.Commands;
import org.jline.builtins.Less;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Very dumb class
 */
public class LessCommand extends AbstractCommand {

    @Option(names = {"e", "-quit-at-eof"})
    private boolean quit_at_eof;

    @Option(names = {"E", "-QUIT-AT-EOF"})
    private boolean QUIT_AT_EOF;

    @Option(names = {"F", "-quit-if-one-screen"})
    private boolean quit_if_one_screen;

    @Option(names = {"q", "-quiet", "-silent "})
    private boolean quiet;

    @Option(names = {"Q", "-QUIET", "-SILENT"})
    private boolean QUIET;

    @Option(names = {"S", "-chop-long-lines"})
    private boolean chop_long_lines;

    @Option(names = {"i", "-ignore-case"})
    private boolean ignore_case;

    @Option(names = {"I", "-IGNORE-CASE"})
    private boolean IGNORE_CASE;

    @Option(names = {"x", "-tabs"}, hasArgument = true)
    private String tabs;

    @Option(names = {"N", "-LINE-NUMBERS"})
    private boolean LINE_NUMBERS;

    @Option(names = {"Y", "-syntax"}, hasArgument = true)
    private String syntax;

    @Option(names = {     "-no-init"})
    private boolean no_init;

    @Option(names = {     "-no-keypad"})
    private boolean no_keypad;

    @Option(names = {     "-ignorercfiles"})
    private boolean ignorercfiles;

    @Option(names = {"H", "-historylog"}, hasArgument = true)
    private String historylog;

    @VaArgs
    private String[] sources;

    @Override
    public int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        String[] options = getOptions();

        try {
            Commands.less(SokoShellHelper.INSTANCE.getTerminal(),
                    in, out, err,
                    Path.of(""), options);
        } catch (Exception e) {
            err.println(e);

            return FAILURE;
        }

        return SUCCESS;
    }

    private String[] getOptions() {
        List<String> options = new ArrayList<>();

        if (quit_at_eof) options.add("-e");
        if (QUIT_AT_EOF) options.add("-E");
        if (quit_if_one_screen) options.add("-F");
        if (quiet) options.add("-q");
        if (QUIET) options.add("-Q");
        if (chop_long_lines) options.add("-S");
        if (ignore_case) options.add("-i");
        if (IGNORE_CASE) options.add("-I");
        if (tabs != null) {
            options.add("-x");
            options.add(tabs);
        }
        if (LINE_NUMBERS) options.add("-N");
        if (syntax != null) {
            options.add("-Y");
            options.add(syntax);
        }
        if (no_init) options.add("--no-init");
        if (no_keypad) options.add("--no-keypad");
        if (ignorercfiles) options.add("-ignorercfiles");
        if (historylog != null) {
            options.add("-H");
            options.add(historylog);
        }

        options.addAll(Arrays.asList(sources));

        return options.toArray(new String[0]);
    }

    @Override
    public String getName() {
        return "less";
    }

    @Override
    public String getUsage() {
        return String.join("\n", Less.usage());
    }

    @Override
    public boolean addHelp() {
        return true;
    }
}
