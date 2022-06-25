package fr.valax.args;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public interface Redirect {

    Redirect NONE = new Redirect() {};
    Redirect ERROR_IN_OUTPUT = new ErrorInStdOut();
    InputFile INPUT_FILE = new InputFile();
    OutputFile OUTPUT_FILE = new OutputFile();

    default InputStream redirectIn(InputStream in) throws IOException {
        return in;
    }

    default PrintStream redirectOut(PrintStream out, PrintStream err) throws IOException {
        return out;
    }

    default PrintStream redirectErr(PrintStream out, PrintStream err) throws IOException {
        return err;
    }


    class InputFile implements Redirect {

        private Path path;

        @Override
        public InputStream redirectIn(InputStream in) throws IOException {
            return new BufferedInputStream(Files.newInputStream(path));
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }
    }

    class OutputFile implements Redirect {

        public static final int STD_OUT = 1;
        public static final int STD_ERR = 2;
        public static final int BOTH = 4;

        private Path out;
        private int redirect;
        private boolean append;

        @Override
        public PrintStream redirectOut(PrintStream out, PrintStream err) throws IOException {
            if ((redirect & STD_OUT) != 0) {
                return createPrintStream();
            } else {
                return Redirect.super.redirectOut(out, err);
            }
        }

        @Override
        public PrintStream redirectErr(PrintStream out, PrintStream err) throws IOException{
            if ((redirect & STD_ERR) != 0) {
                return createPrintStream();
            } else {
                return Redirect.super.redirectErr(out, err);
            }
        }

        private PrintStream createPrintStream() throws IOException {
            OutputStream os;
            if (append) {
                os = Files.newOutputStream(this.out, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                os = Files.newOutputStream(this.out, StandardOpenOption.CREATE);
            }

            return new PrintStream(os);
        }

        public void set(Path out, int redirect, boolean append) {
            this.out = out;
            this.redirect = redirect;
            this.append = append;
        }

        public Path getOut() {
            return out;
        }

        public void setOut(Path out) {
            this.out = out;
        }

        public int getRedirect() {
            return redirect;
        }

        public void setRedirect(int redirect) {
            this.redirect = redirect;
        }

        public boolean isAppend() {
            return append;
        }

        public void setAppend(boolean append) {
            this.append = append;
        }

        public boolean isRedirectingStdOut() {
            return (redirect & STD_OUT) != 0;
        }

        public boolean isRedirectingStdErr() {
            return (redirect & STD_ERR) != 0;
        }
    }

    class ErrorInStdOut implements Redirect {

        @Override
        public PrintStream redirectErr(PrintStream out, PrintStream err) {
            return out;
        }
    }
}
