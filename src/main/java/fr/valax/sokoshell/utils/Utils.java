package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Utils {

    public static final ExecutorService SOKOSHELL_EXECUTOR = Executors.newCachedThreadPool();
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR =
            Executors.newScheduledThreadPool(
                    Math.max(1, Runtime.getRuntime().availableProcessors() / 4));

    public static final Comparator<AttributedString> ATTRIBUTED_STRING_COMPARATOR =
            Comparator.comparing(AttributedString::toString);

    public static int nDigit(int v) {
        if (v == 0)  {
            return 1;
        } else if (v < 0) {
            return 1 + nDigit(-v);
        } else {
            return (int) (1 + Math.log10(v));
        }
    }

    public static String getExtension(Path path) {
        String filename = path.getFileName().toString();

        int dot = filename.lastIndexOf('.');

        if (dot < 0) {
            return "";
        } else {
            return filename.substring(dot + 1);
        }
    }

    public static OptionalInt parseInt(String str) {
        try {
            return OptionalInt.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static void shutdownExecutors() {
        SOKOSHELL_EXECUTOR.shutdown();
        SCHEDULED_EXECUTOR.shutdown();
    }
}
