package fr.valax.sokoshell.utils;

import org.jline.builtins.Completers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Utils {

    public static final ExecutorService SOKOSHELL_EXECUTOR = Executors.newCachedThreadPool();
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR =
            Executors.newScheduledThreadPool(
                    Math.max(1, Runtime.getRuntime().availableProcessors() / 4));

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

    public static <T> void resize(List<T> list, int size) {
        if (list.size() < size) {
            while (list.size() != size) {
                list.add(null);
            }
        } else if (list.size() > size) {
            while (list.size() != size) {
                list.remove(size);
            }
        }
    }

    public static void shutdownExecutors() {
        SOKOSHELL_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();
    }
}
