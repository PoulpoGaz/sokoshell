package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.utils.CommandLineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CommandLineTest {

    private static final String FILE_1 = """
            Lorem ipsum
            Lorem ipsum
            ipsum
            sum sum sum hello
            Lorem ipsum
            """;

    private static final String FILE_2 = """
            hello world
            lorem ipsum
            """;

    @Test
    void unrecognizedCommand() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stdErr));

        Assertions.assertEquals(Command.FAILURE, cli.execute("a-super-cool-command"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("cli: a-super-cool-command: command not found\n", stdErr.toString());

        stdout.reset();
        stdErr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("a-super-cool-command; echo hello; ffff; echo hello2"));
        Assertions.assertEquals("hello\nhello2\n", stdout.toString());
        Assertions.assertEquals("cli: a-super-cool-command: command not found\ncli: ffff: command not found\n", stdErr.toString());
    }

    @Test
    void echoTest() throws CommandLineException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(baos));

        Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world"));
        Assertions.assertEquals("hello world\n", baos.toString());
    }

    @Test
    void catTest() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(baos));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat src/test/file1.txt"));
            Assertions.assertEquals(FILE_1, baos.toString());

            baos.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat src/test/file1.txt src/test/file2.txt"));
            Assertions.assertEquals(FILE_1 + FILE_2, baos.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void grepTest() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(baos));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("grep \"Lorem\" src/test/file1.txt"));
            Assertions.assertEquals("""
                                    Lorem ipsum
                                    Lorem ipsum
                                    Lorem ipsum
                                    """, baos.toString());

            baos.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("grep hello src/test/file1.txt src/test/file2.txt"));
            Assertions.assertEquals("""
            src/test/file1.txt: sum sum sum hello
            src/test/file2.txt: hello world
            """, baos.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void input() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file1.txt"));
            Assertions.assertEquals(FILE_1, stdout.toString());

            stdout.reset();
            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file2.txt"));
            Assertions.assertEquals(FILE_2, stdout.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void redirectErr() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < blablabla"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: blablabla: No such file or directory\n", stdErr.toString());

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("2>&1 cat blablabla"));
            Assertions.assertEquals("", stdErr.toString());
            Assertions.assertEquals("cat: blablabla: No such file or directory\n", stdout.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stdOutToFile() throws CommandLineException, IOException {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world > src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("hello world\n", Files.readString(Path.of("src/test/file1.txt")));

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < bla > src/test/file2.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: bla: No such file or directory\n", stdErr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stdOutToFileAppend() throws CommandLineException, IOException {
        Path out = Path.of("src/test/file1.txt");

        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world >> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("hello world\n", Files.readString(out));

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello peter >> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("hello world\nhello peter\n", Files.readString(out));
        } finally {
            delete(out);
        }
    }

    @Test
    void stdErrToFile() throws CommandLineException, IOException {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat t 2> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("cat: t: No such file or directory\n", Files.readString(Path.of("src/test/file1.txt")));

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < bla 2> src/test/file2.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: bla: No such file or directory\n", stdErr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stdErrToFileAppend() throws CommandLineException, IOException {
        Path out = Path.of("src/test/file1.txt");

        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat t 2>> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("cat: t: No such file or directory\n", Files.readString(out));

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("grep \"b\" file 2>> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            Assertions.assertEquals("cat: t: No such file or directory\ngrep: file: No such file or directory\n", Files.readString(out));
        } finally {
            delete(out);
        }
    }

    @Test
    void pipe() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world | cat"));
            Assertions.assertEquals("hello world\n", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file1.txt | grep o | grep h"));
            Assertions.assertEquals("sum sum sum hello\n", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void commandSeparator() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stdErr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world; echo hello peter"));
            Assertions.assertEquals("hello world\nhello peter\n", stdout.toString());
            Assertions.assertEquals("", stdErr.toString());

            stdout.reset();
            stdErr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < test; cat < src/test/file1.txt; cat < src/test/file1.txt | grep o | grep h"));
            Assertions.assertEquals(FILE_1 + "sum sum sum hello\n", stdout.toString());
            Assertions.assertEquals("cli: test: No such file or directory\n", stdErr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    private void writeTestFiles() {
        write(FILE_1, Path.of("src/test/file1.txt"));
        write(FILE_2, Path.of("src/test/file2.txt"));
    }

    private void deleteTestFiles() {
        delete(Path.of("src/test/file1.txt"));
        delete(Path.of("src/test/file2.txt"));
    }

    private void write(String file, Path out) {
        try {
            Files.writeString(out, file, StandardOpenOption.CREATE);
        } catch (IOException e) {
            // ignored
        }
    }

    private void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // ignored
        }
    }
}
