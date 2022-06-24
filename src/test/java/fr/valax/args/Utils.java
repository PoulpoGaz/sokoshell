package fr.valax.args;

import fr.valax.args.api.Command;
import fr.valax.args.api.VaArgs;
import fr.valax.args.utils.CommandLineException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class Utils {

    public static CommandLine newCLI() throws CommandLineException {
        return new CommandLineBuilder()
                .addDefaultConverters()
                .addCommand(new Echo())
                .addCommand(new Cat())
                .addCommand(new Grep())
                .build();
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

        @VaArgs
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
            return false;
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
