package fr.valax.sokoshell.utils;

import org.jline.builtins.Completers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {

    public static final ExecutorService SOKOSHELL_EXECUTOR = Executors.newCachedThreadPool();

    public static final Completers.FileNameCompleter FILE_NAME_COMPLETER = new Completers.FileNameCompleter();

    public static int nDigit(int v) {
        if (v == 0)  {
            return 1;
        } else if (v < 0) {
            return 1 + nDigit(-v);
        } else {
            return (int) (1 + Math.log10(v));
        }
    }
}
