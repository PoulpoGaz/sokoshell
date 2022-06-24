package fr.valax.args;

import fr.valax.args.api.*;
import fr.valax.args.utils.ArgsUtils;
import fr.valax.args.utils.CommandLineException;
import fr.valax.args.utils.TypeException;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Colors;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Locale;

public class Test {

    @org.junit.jupiter.api.Test
    void test() throws CommandLineException {
        CommandLine cli = new CommandLineBuilder()
                .addDefaultConverters()
                .subCommand(new Init())
                    .addCommand(new Proj())
                    .addCommand(new Exec())
                    .endSubCommand()
                .subCommand(new Print())
                    .addCommand(new CoolPrint())
                    .endSubCommand()
                .addCommand(new Fibo())
                .addCommand(new Fibo2())
                .build();

        exec(cli, "init");
        exec(cli, "init proj");
        exec(cli, "init fdgs");
        exec(cli, "init -gdf");
        exec(cli, "init proj -dsg");
        exec(cli, "init proj -n hello");
        exec(cli, "init proj -n hello -l home");
        exec(cli, "init exec -n hello -l home");
        exec(cli, "init exec -n hello -l home --exec RUN");
        exec(cli, "init exec -n hello -l home --exec BFDBSBS");
        exec(cli, "print hello world!");
        exec(cli, "print -f \"you have %s IQ\" 0%n");
        exec(cli, "print cool H E L L O W O R L D");
        exec(cli, "fibo -n 0");
        exec(cli, "fibo -n 1");
        exec(cli, "fibo -n 2");
        exec(cli, "fibo -n 20");
        exec(cli, "fibo");
        exec(cli, "fibo2");
        exec(cli, "fibo2 -n 20");

        exec(cli, "fibo2 --help");
        exec(cli, "init proj -h");
    }

    private void exec(CommandLine cli, String cmd) throws CommandLineException {
        System.out.println("==> " + cmd);
        cli.execute(ArgsUtils.splitQuoted(cmd));
    }

    private static class Init implements Command {

        @Override
        public String getName() {
            return "init";
        }

        @Override
        public String getUsage() {
            return "init the project";
        }

        @Override
        public boolean addHelp() {
            return false;
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            System.out.println("Target needed");
            return SUCCESS;
        }
    }

    private static class Proj implements Command {

        @Option(names = {"n", "-name"}, hasArgument = true, optional = false)
        private String projName;

        @Option(names = {"l", "-location"}, hasArgument = true)
        private Path location;

        @Override
        public String getName() {
            return "proj";
        }

        @Override
        public String getUsage() {
            return "Init a new project";
        }

        @Override
        public boolean addHelp() {
            return true;
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (location != null) {
                System.out.printf("Init project %s at %s%n", projName, location);
            } else {
                System.out.printf("Init project %s%n", projName);
            }

            return SUCCESS;
        }
    }

    private static class Exec implements Command {

        @Option(names = {"n", "-name"}, hasArgument = true, optional = false)
        private String projName;

        @Option(names = {"l", "-location"}, hasArgument = true)
        private Path location;

        @Option(names = {"e", "-exec"}, hasArgument = true, converter = Execution.Converter.class)
        private Execution exec;

        @Override
        public String getName() {
            return "exec";
        }

        @Override
        public String getUsage() {
            return "Init an execution";
        }

        @Override
        public boolean addHelp() {
            return true;
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            System.out.printf("In %s at %s%n", projName, location);
            if (exec == null) {
                System.out.println("No execution specified. Setting RUN");

                exec = Execution.RUN;
            }

            System.out.println("Initializing: " + exec.name().toLowerCase(Locale.ROOT));

            return SUCCESS;
        }

    }

    private enum Execution {
        RUN,
        DEBUG,
        COVERAGE,
        PROFILER;

        public static class Converter implements TypeConverter<Execution> {

            @Override
            public Execution convert(String value) throws TypeException {
                if (value == null) {
                    return null;
                }

                return switch (value.toLowerCase(Locale.ROOT)) {
                    case "run" -> RUN;
                    case "debug" -> DEBUG;
                    case "coverage" -> COVERAGE;
                    case "profiler" -> PROFILER;
                    default -> throw new TypeException("Unknown execution: " + value);
                };
            }
        }
    }

    private static class Print implements Command {

        @Option(names = {"f", "-format"}, hasArgument = true)
        private String format;

        @VaArgs
        private String[] vaargs;

        @Override
        public String getName() {
            return "print";
        }

        @Override
        public String getUsage() {
            return "print";
        }

        @Override
        public boolean addHelp() {
            return false;
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (format != null) {
                out.printf(format, (Object[]) vaargs);
            } else {
                out.println(String.join(" ", vaargs));
            }

            return SUCCESS;
        }
    }

    private static class CoolPrint implements Command {

        @Option(names = {"f", "-format"}, hasArgument = true)
        private String format;

        @VaArgs(converter = CoolPrint.Converter.class)
        private String[] vaargs;

        @Override
        public String getName() {
            return "cool";
        }

        @Override
        public String getUsage() {
            return "coooooool print";
        }

        @Override
        public boolean addHelp() {
            return false;
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (format != null) {
                System.out.printf(format, (Object[]) vaargs);
            } else {
                System.out.println(String.join(" ", vaargs));
            }

            return SUCCESS;
        }

        public static class Converter implements TypeConverter<String> {

            @Override
            public String convert(String value) {
                if (value == null) {
                    return new AttributedStringBuilder()
                            .style(AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("NULL")
                            .toAnsi();
                } else {
                    return new AttributedStringBuilder()
                            .style(AttributedStyle.DEFAULT.foreground(Colors.rgbColor((int) (Math.random() * 256))))
                            .append(value)
                            .toAnsi();
                }
            }
        }
    }

    private static class Fibo implements Command {

        @Option(names = "n", hasArgument = true)
        private Integer n;

        private int fibo(int nterm, int lastnterm, int n) {
            if (n == 0) {
                return nterm;
            } else {
                return fibo(nterm + lastnterm, nterm, n - 1);
            }
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err){
            if (n == null) {
                System.out.println(-1);
                return FAILURE;
            } else {
                int f = fibo(0, 1, n);
                System.out.println(f);

                return SUCCESS;
            }
        }

        @Override
        public String getName() {
            return "fibo";
        }

        @Override
        public String getUsage() {
            return "compute fibo n";
        }

        @Override
        public boolean addHelp() {
            return true;
        }
    }

    private static class Fibo2 implements Command {

        @Option(names = "n", hasArgument = true)
        private int n;

        private int fibo(int nterm, int lastnterm, int n) {
            if (n == 0) {
                return nterm;
            } else {
                return fibo(nterm + lastnterm, nterm, n - 1);
            }
        }

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            int f = fibo(0, 1, n);
            System.out.println(f);

            return SUCCESS;
        }

        @Override
        public String getName() {
            return "fibo2";
        }

        @Override
        public String getUsage() {
            return "compute fibo n";
        }

        @Override
        public boolean addHelp() {
            return true;
        }
    }
}
