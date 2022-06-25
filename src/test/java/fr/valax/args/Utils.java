package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.api.Option;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.CommandLineException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    private static final List<String> installed = new ArrayList<>();

    public static CommandLine newCLI() throws CommandLineException {
        return new CommandLineBuilder()
                .addDefaultConverters()
                .addCommand(new Echo())
                .addCommand(new Cat())
                .addCommand(new Grep())
                .subCommand(new Apt())
                    .addCommand(new Install())
                    .addCommand(new Remove())
                    .addCommand(new ListCmd())
                    .endSubCommand()
                .addCommand(new Add())
                .build();
    }

    public static class Add implements Command {

        @Option(names = "a", hasArgument = true)
        private int a;

        @Option(names = "b", hasArgument = true)
        private int b;

        @Option(names = "c", hasArgument = true)
        private int c;

        @Option(names = {"-stdin", "i"})
        private boolean readInput;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (readInput) {
                try {
                    byte[] bytes = in.readAllBytes();
                    String str = new String(bytes);

                    // remove line separator
                    int i = Integer.parseInt(str.replaceAll(System.lineSeparator(), ""));

                    out.println(a + b + c + i);
                } catch (IOException | NumberFormatException e) {
                    e.printStackTrace(err);
                }
            } else {
                out.println(a + b + c);
            }

            return 0;
        }

        @Override
        public String getName() {
            return "add";
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class Apt implements Command {

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            out.println("apt");
            return 0;
        }

        @Override
        public String getName() {
            return "apt";
        }

        @Override
        public String getUsage() {
            return "apt-usage";
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class Install implements Command {

        @Option(names = {"-package", "p"}, optional = false, allowDuplicate = true, hasArgument = true)
        private String[] packages;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            for (String str : packages) {
                out.println("Installing " + str);
                installed.add(str);
            }

            return 0;
        }

        @Override
        public String getName() {
            return "install";
        }

        @Override
        public String getUsage() {
            return "install";
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class Remove implements Command {

        @Option(names = {"-package", "p"}, optional = false, allowDuplicate = true, hasArgument = true)
        private String[] packages;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            for (String p : packages) {
                out.println("Removing " + p);
                installed.remove(p);
            }

            return 0;
        }

        @Override
        public String getName() {
            return "remove";
        }

        @Override
        public String getUsage() {
            return "remove packages";
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class ListCmd implements Command {

        @Option(names = "-installed")
        private boolean installed;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (installed) {
                for (String str : Utils.installed) {
                    out.println(str);
                }
            } else {
                out.println("bla bla bla");
            }

            return 0;
        }

        @Override
        public String getName() {
            return "list";
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class Echo implements Command {

        @VaArgs
        private String[] args;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            for (int i = 0; i < args.length; i++) {
                out.print(args[i]);

                if (i + 1 < args.length) {
                    out.print(' ');
                }
            }

            out.println();

            return SUCCESS;
        }

        @Override
        public String getName() {
            return "echo";
        }

        @Override
        public String getUsage() {
            return "echo";
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }

    public static class Cat implements Command {

        @VaArgs(description = "Files to read")
        private Path[] files;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            int ret = SUCCESS;

            if (files.length == 0) {

                try {
                    in.transferTo(out);
                } catch (IOException e) {
                    e.printStackTrace(err);

                    ret = FAILURE;
                }

            } else {

                try {
                    for (Path file : files) {

                        if (!Files.exists(file)) {
                            err.printf("cat: %s: No such file or directory%n", file);
                            ret = FAILURE;
                        } else {
                            InputStream is = Files.newInputStream(file);
                            is.transferTo(out);
                            is.close();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace(err);

                    ret = FAILURE;
                }
            }

            return ret;
        }

        @Override
        public String getName() {
            return "cat";
        }

        @Override
        public String getUsage() {
            return "cat";
        }

        @Override
        public boolean addHelp() {
            return true;
        }
    }

    public static class Grep implements Command {

        @VaArgs
        private String[] args;

        @Override
        public int execute(InputStream in, PrintStream out, PrintStream err) {
            if (args.length == 0) {
                err.println("grep error");
            } else {

                String regex = args[0];
                Pattern pattern = Pattern.compile(regex);

                if (args.length == 1) {
                    try {
                        findLinesMatching(new BufferedReader(new InputStreamReader(in)), null, false, pattern, out);
                    } catch (IOException e) {
                        e.printStackTrace(err);

                        return FAILURE;
                    }
                } else {
                    try {
                        for (int i = 1; i < args.length; i++) {
                            Path file = Path.of(args[i]);

                            if (!Files.exists(file)) {
                                err.printf("grep: %s: No such file or directory%n", file);
                            } else {
                                BufferedReader br = Files.newBufferedReader(file);
                                findLinesMatching(br, args[i], args.length > 2, pattern, out);
                                br.close();
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace(err);

                        return FAILURE;
                    }

                }
            }

            return 0;
        }

        private void findLinesMatching(BufferedReader br, String file, boolean showFile, Pattern pattern, PrintStream out) throws IOException {
            String line;

            while ((line = br.readLine()) != null) {
                if (pattern.matcher(line).find()) {
                    if (showFile) {
                        out.print(file);
                        out.print(": ");
                    }


                    out.println(line);
                }
            }
        }

        @Override
        public String getName() {
            return "grep";
        }

        @Override
        public String getUsage() {
            return "grep";
        }

        @Override
        public boolean addHelp() {
            return false;
        }
    }
}
