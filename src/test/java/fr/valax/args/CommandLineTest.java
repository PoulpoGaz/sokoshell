package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.utils.CommandLineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

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
    void argError() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.FAILURE, cli.execute("apt -bla"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("apt: bla: No such option\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("apt vaargs"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("apt: VaArgs not allowed\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("add -a 5 -a 10 -a 15"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("add: a: Duplicate parameter\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("add -a 5 -b 10 -c << hello"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("add: c: expecting word\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("apt remove -p"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("apt remove: package: Parameter required\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("apt remove"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("apt remove: package: required\n", stderr.toString());
    }

    @Test
    void aptTest() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt"));
        Assertions.assertEquals("apt\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt install -p hello\\ world -p test"));
        Assertions.assertEquals("Installing hello world\nInstalling test\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt list --installed"));
        Assertions.assertEquals("hello world\ntest\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt list"));
        Assertions.assertEquals("bla bla bla\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt remove -p \"hello world\""));
        Assertions.assertEquals("Removing hello world\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("apt list --installed"));
        Assertions.assertEquals("test\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());
    }

    @Test
    void pipeAndArgs() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.SUCCESS, cli.execute("add -a 5 -b \\-10"));
        Assertions.assertEquals("-5\n", stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("add -a 5 -c 15 | add -a 1000 -b \\-1000 -c 5 -i"));
        Assertions.assertEquals("", stderr.toString());
        Assertions.assertEquals("25\n", stdout.toString());

        stdout.reset();
        stderr.reset();

        Path path = Path.of("src/test/add.txt");
        try {
            write("255", path);

            Assertions.assertEquals(Command.SUCCESS, cli.execute("< src/test/add.txt add -i -a 5 -c 15 | add -a 1000 -b \\-1000 -c 5 -i"));
            Assertions.assertEquals("", stderr.toString());
            Assertions.assertEquals("280\n", stdout.toString());

            stdout.reset();
            stderr.reset();
        } finally {
            delete(path);
        }
    }

    @Test
    void typeException() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.FAILURE, cli.execute("add -a abc"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("add: java.lang.NumberFormatException: For input string: \"abc\"\n", stderr.toString());

        stdout.reset();
        stderr.reset();
    }

    @Test
    void help() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.SUCCESS, cli.execute("cat --help"));
        Assertions.assertEquals("""
                Command: cat
                Usage: cat
                                
                Vaargs: Files to read\s
                -h, --help          Print help\s

                """, stdout.toString());
        Assertions.assertEquals("", stderr.toString());

        stdout.reset();
        stderr.reset();
    }

    @Test
    void unrecognizedCommand() throws CommandLineException, IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        CommandLine cli = Utils.newCLI();
        cli.setStdOut(new PrintStream(stdout));
        cli.setStdErr(new PrintStream(stderr));

        Assertions.assertEquals(Command.FAILURE, cli.execute("a-super-cool-command"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("cli: a-super-cool-command: command not found\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.SUCCESS, cli.execute("a-super-cool-command; echo hello; ffff; echo hello2"));
        Assertions.assertEquals("hello\nhello2\n", stdout.toString());
        Assertions.assertEquals("cli: a-super-cool-command: command not found\ncli: ffff: command not found\n", stderr.toString());

        stdout.reset();
        stderr.reset();

        Assertions.assertEquals(Command.FAILURE, cli.execute("--hello"));
        Assertions.assertEquals("", stdout.toString());
        Assertions.assertEquals("cli: No root command exists\n", stderr.toString());
    }

    @Test
    void endOptionParsing() throws CommandLineException, IOException {
        StringBuilder expected = new StringBuilder();

        int n = 1;
        for (Iterator<String> it = FILE_1.lines().iterator(); it.hasNext(); ) {
            String line = it.next();

            expected.append(n).append(": ").append(line).append('\n');
            n++;
        }

        n = 1;
        for (Iterator<String> it = FILE_2.lines().iterator(); it.hasNext(); ) {
            String line = it.next();

            expected.append(n).append(": ").append(line).append('\n');
            n++;
        }


        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat -n src/test/file1.txt -- -n src/test/file2.txt"));
            Assertions.assertEquals(expected.toString(), stdout.toString());
            Assertions.assertEquals("cat: -n: No such file or directory\n", stderr.toString());
        } finally {
            deleteTestFiles();
        }
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
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file1.txt"));
            Assertions.assertEquals(FILE_1, stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            stdout.reset();
            stderr.reset();
            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file2.txt"));
            Assertions.assertEquals(FILE_2, stdout.toString());
            Assertions.assertEquals("", stderr.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void redirectErr() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < blablabla"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: blablabla: No such file or directory\n", stderr.toString());

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("2>&1 cat blablabla"));
            Assertions.assertEquals("", stderr.toString());
            Assertions.assertEquals("cat: blablabla: No such file or directory\n", stdout.toString());

        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stdoutToFile() throws CommandLineException, IOException {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world > src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            Assertions.assertEquals("hello world\n", Files.readString(Path.of("src/test/file1.txt")));

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < bla > src/test/file2.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: bla: No such file or directory\n", stderr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stdoutToFileAppend() throws CommandLineException, IOException {
        Path out = Path.of("src/test/file1.txt");

        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world >> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            Assertions.assertEquals("hello world\n", Files.readString(out));

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello peter >> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            Assertions.assertEquals("hello world\nhello peter\n", Files.readString(out));
        } finally {
            delete(out);
        }
    }

    @Test
    void stderrToFile() throws CommandLineException, IOException {
        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat t 2> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            Assertions.assertEquals("cat: t: No such file or directory\n", Files.readString(Path.of("src/test/file1.txt")));

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat < bla 2> src/test/file2.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("cli: bla: No such file or directory\n", stderr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void stderrToFileAppend() throws CommandLineException, IOException {
        Path out = Path.of("src/test/file1.txt");

        try {
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.FAILURE, cli.execute("cat t 2>> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            Assertions.assertEquals("cat: t: No such file or directory\n", Files.readString(out));

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("grep \"b\" file 2>> src/test/file1.txt"));
            Assertions.assertEquals("", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

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
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world | cat"));
            Assertions.assertEquals("hello world\n", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < src/test/file1.txt | grep o | grep h"));
            Assertions.assertEquals("sum sum sum hello\n", stdout.toString());
            Assertions.assertEquals("", stderr.toString());
        } finally {
            deleteTestFiles();
        }
    }

    @Test
    void commandSeparator() throws CommandLineException, IOException {
        try {
            writeTestFiles();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            CommandLine cli = Utils.newCLI();
            cli.setStdOut(new PrintStream(stdout));
            cli.setStdErr(new PrintStream(stderr));

            Assertions.assertEquals(Command.SUCCESS, cli.execute("echo hello world; echo hello peter"));
            Assertions.assertEquals("hello world\nhello peter\n", stdout.toString());
            Assertions.assertEquals("", stderr.toString());

            stdout.reset();
            stderr.reset();

            Assertions.assertEquals(Command.SUCCESS, cli.execute("cat < test; cat < src/test/file1.txt; cat < src/test/file1.txt | grep o | grep h"));
            Assertions.assertEquals(FILE_1 + "sum sum sum hello\n", stdout.toString());
            Assertions.assertEquals("cli: test: No such file or directory\n", stderr.toString());
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

    private void write(String str, Path out) {
        try {
            Files.writeString(out, str, StandardOpenOption.CREATE);
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
