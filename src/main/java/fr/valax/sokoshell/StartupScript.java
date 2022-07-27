package fr.valax.sokoshell;

import fr.valax.args.CommandLine;
import fr.valax.args.utils.CommandLineException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StartupScript {

    private static final PrintStream NULL_PRINT_STREAM = new PrintStream(OutputStream.nullOutputStream());

    private final CommandLine cli;

    private PrintStream stdOut;
    private PrintStream stdErr;

    private char[] chars;
    private int index;

    public StartupScript(CommandLine cli) {
        this.cli = cli;
    }

    public void run(Path path) throws IOException, CommandLineException {
        if (Files.notExists(path)) {
            return;
        }

        stdOut = cli.getStdOut();
        stdErr = cli.getStdErr();

        try (BufferedReader br = Files.newBufferedReader(path)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                execute(line);
            }
        }

        cli.setStdOut(stdOut);
        cli.setStdErr(stdErr);
    }

    private void execute(String line) throws IOException, CommandLineException {
        chars = line.toCharArray();
        index = 0;

        skipWhitespace();
        if (chars[index] != '#') { // line is commented
            if (chars[index] == '@') {
                index++;
                executeSpecialCommand();
            } else {
                cli.execute(line);
            }
        }
    }

    private void executeSpecialCommand() throws IOException {
        String command = nextWord();

        if (command.equals("echo")) {
            String onOff = lastWord();

            echo(!onOff.equals("off"));
        } else {
            throw new IOException("Unknown special command: " + command);
        }
    }


    private void echo(boolean on) {
        if (on) {
            cli.setStdOut(stdOut);
            cli.setStdErr(stdErr);
        } else {
            cli.setStdOut(NULL_PRINT_STREAM);
            cli.setStdErr(NULL_PRINT_STREAM);
        }
    }



    private String lastWord() throws IOException {
        skipWhitespace();
        String word = nextWord();
        skipWhitespace();

        if (index < chars.length) {
            throw new IOException("Not the last word");
        }

        return word;
    }

    private String nextWord() {
        skipWhitespace();

        StringBuilder sb = new StringBuilder();

        for (; index < chars.length; index++) {
            if (Character.isWhitespace(chars[index])) {
                break;
            }

            sb.append(chars[index]);
        }

        return sb.toString();
    }

    private void skipWhitespace() {
        while (index < chars.length && Character.isWhitespace(chars[index])) {
            index++;
        }
    }
}
