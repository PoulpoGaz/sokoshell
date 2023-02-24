package fr.valax.sokoshell.commands.unix;

import fr.valax.args.CommandLine;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.jline.FileNameCompleter;
import fr.valax.sokoshell.commands.AbstractCommand;
import fr.valax.sokoshell.utils.Utils;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Based on GNU wc
 */
public class WordCount extends AbstractCommand {

    @Option(names = {"m", "chars"}, description = "print the character counts")
    private boolean countChars;

    @Option(names = {"w", "words"}, description = "print the word counts")
    private boolean countWords;

    @Option(names = {"l", "lines"}, description = "print the newline counts")
    private boolean countLines;

    @VaArgs
    private String[] vaArgs;

    @Override
    protected int executeImpl(InputStream in, PrintStream out, PrintStream err) throws InvalidArgument {
        // print chars, if explicitly set by the user
        // or if the user didn't say anything
        boolean printChars = countChars || (!countWords && !countLines);
        boolean printWords = countWords || (!countChars && !countLines);
        boolean printLines = countLines || (!countChars && !countWords);

        countChars = printChars;
        countWords = printWords;
        countLines = printLines;

        List<CountResult> results = new ArrayList<>();

        try {
            if (vaArgs.length == 0) {
                results.add(count("", in)); // force empty name
            } else {
                for (String vaArg : vaArgs) {
                    CountResult r;
                    if (vaArg.equals("-")) {
                        r = count("-", in);
                    } else {
                        FileInputStream is = new FileInputStream(vaArg);
                        r = count(vaArg, is);
                    }

                    results.add(r);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(err);
            return FAILURE;
        }

        printResults(results);

        return 0;
    }

    private CountResult count(String inputName, InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        CountResult r = new CountResult(inputName);

        String line;
        while ((line = br.readLine()) != null) {
            r.add(line.length(), countWords(line), 1);
        }

        br.close();

        return r;
    }

    private int countWords(String line) {
        int numWords = 0;
        boolean inWord = false;

        for (int i = 0; i < line.length(); i++) {
            if (Character.isWhitespace(line.charAt(i))) {
                inWord = false;
            } else {
                if (!inWord) {
                    numWords++;
                }

                inWord = true;
            }
        }

        return numWords;
    }

    private void printResults(List<CountResult> results) {
        long totalLines = 0;
        long totalWords = 0;
        long totalCharacters = 0;

        long maxLines = 0;
        long maxWords = 0;
        long maxCharacters = 0;

        for (CountResult r : results) {
            totalLines += r.getNumLines();
            totalWords += r.getNumWords();
            totalCharacters += r.getNumChars();

            maxLines = Math.max(maxLines, r.getNumLines());
            maxWords = Math.max(maxWords, r.getNumWords());
            maxCharacters = Math.max(maxCharacters, r.getNumChars());
        }

        int columnSize = 0;
        if (countChars) {
            columnSize = Utils.nDigit(maxCharacters);
        }
        if (countWords) {
            columnSize = Math.max(Utils.nDigit(maxWords), columnSize);
        }
        if (countLines) {
            columnSize = Math.max(Utils.nDigit(maxLines), columnSize);
        }

        columnSize += 4;

        String format = "%" + columnSize + "d";
        for (CountResult r : results) {
            print(format, r);
        }

        if (results.size() > 1) {
            print(format, totalLines, totalWords, totalCharacters, "total");
        }
    }

    private void print(String format, CountResult r) {
        print(format, r.getNumLines(), r.getNumWords(), r.getNumChars(), r.getInputName());
    }

    private void print(String format, long numLines, long numWords, long numChars, String name) {
        if (countLines) {
            System.out.printf(format, numLines);
        }
        if (countWords) {
            System.out.printf(format, numWords);
        }
        if (countChars) {
            System.out.printf(format, numChars);
        }

        if (name.isBlank()) {
            System.out.println();
        } else {
            System.out.println(" " + name);
        }
    }

    @Override
    public String getName() {
        return "wc";
    }

    @Override
    public String getShortDescription() {
        return "print newline, word, and byte counts for each file";
    }

    @Override
    public String[] getUsage() {
        return new String[] {
                "Print newline, word, and character counts for each FILE, and a total line if more than one FILE is specified.  " +
                        "A word is a non-zero-length sequence of characters delimited by white space.",
                "With no FILE, or when FILE is -, read standard inputName."
        };
    }

    @Override
    public void complete(LineReader reader, String commandString, CommandLine.CommandSpec command,
                         List<Candidate> candidates, CommandLine.OptionSpec option, String argument) {
        if (option == null) {
            FileNameCompleter.INSTANCE.complete(reader, Objects.requireNonNullElse(argument, ""), candidates);
        }
    }

    private static class CountResult {

        private final String inputName;
        private long numChars;
        private long numWords;
        private long numLines;

        private CountResult(String inputName) {
            this.inputName = inputName;
        }

        public void add(int numChars, int numWords, int numLines) {
            this.numChars += numChars;
            this.numWords += numWords;
            this.numLines += numLines;
        }

        public String getInputName() {
            return inputName;
        }

        public long getNumChars() {
            return numChars;
        }

        public long getNumWords() {
            return numWords;
        }

        public long getNumLines() {
            return numLines;
        }
    }
}
