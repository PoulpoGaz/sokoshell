package fr.valax.sokoshell.commands.basic;

import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.sokoshell.commands.AbstractCommand;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * only --show-nonprinting is not implemented
 */
public class Cat extends AbstractCommand {

    @Option(names = {"A", "show-all"}, description = "equivalent to -ET")
    private boolean showAll;

    @Option(names = {"b", "number-nonblank"}, description = "number nonempty output lines, overrides -n")
    private boolean numberNonBlank;

    @Option(names = {"E", "show-ends"}, description = "display $ at end of each line")
    private boolean showEnds;

    @Option(names = {"n", "number"}, description = "number all output lines")
    private boolean number;

    @Option(names = {"s", "squeeze-blank"}, description = "suppress repeated empty output lines")
    private boolean squeezeBlank;

    @Option(names = {"T", "show-tabs"}, description = "display TAB characters as ^I")
    private boolean showTabs;

    @VaArgs
    private String[] input;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) {
        if (showAll) {
            showEnds = true;
            showTabs = true;
        }

        if (numberNonBlank) {
            number = false;
        }

        if (input.length == 0) {
            try {
                print(in, out);
            } catch (IOException e) {
                e.printStackTrace(err);
            }

        } else {
            for (String i : input) {
                if (i.equals("-")) {
                    try {
                        print(in, out);
                    } catch (IOException e) {
                        e.printStackTrace(err);
                    }
                } else {
                    Path path = Path.of(i);

                    if (Files.notExists(path)) {
                        err.printf("cat: %s: No such file or directory%n", i);
                        continue;
                    }

                    try (InputStream is = Files.newInputStream(path)) {
                        print(is, out);
                    } catch (IOException e) {
                        e.printStackTrace(err);
                    }
                }
            }
        }

        return 0;
    }


    private void print(InputStream is, PrintStream out) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        int lineNumber = 0;
        boolean lastWasEmpty = false;
        String line;
        while ((line = br.readLine()) != null) {

            boolean blank = line.isBlank();
            if (blank) {
                if (lastWasEmpty && squeezeBlank) {
                    continue;
                }

                if (!numberNonBlank) {
                    lineNumber++;
                }

                lastWasEmpty = true;
            } else {
                lineNumber++;
                lastWasEmpty = false;
            }

            if (number || numberNonBlank && !blank) {
                out.printf("%6d  ", lineNumber);
            }

            if (showTabs) {
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (line.charAt(i) == '\t') {
                        out.print("^I");
                    } else {
                        out.print(c);
                    }
                }
            } else {
                out.print(line);
            }

            if (showEnds) {
                out.println("$");
            } else {
                out.println();
            }
        }
    }

    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public String getShortDescription() {
        return "concatenate files and print on the standard output";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public void completeVaArgs(LineReader reader, String argument, List<Candidate> candidates) {
        FileNameCompleter.INSTANCE.complete(reader, argument, candidates);
    }
}