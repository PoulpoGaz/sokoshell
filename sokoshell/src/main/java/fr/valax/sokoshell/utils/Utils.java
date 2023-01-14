package fr.valax.sokoshell.utils;

import org.jline.utils.AttributedString;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Comparator;
import java.util.OptionalInt;

public class Utils {

    public static final Comparator<AttributedString> ATTRIBUTED_STRING_COMPARATOR =
            Comparator.comparing(AttributedString::toString);

    public static final DateFormat DDMMYYYY_HHMM = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public static void append(Throwable e, Path path) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        append(sw.toString(), path);
    }

    public static void append(String str, Path path) {
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            bw.write(str);
            bw.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public static String getFileName(Path path) {
        String filename = path.getFileName().toString();

        int dot = filename.lastIndexOf('.');

        if (dot < 0) {
            return filename;
        } else {
            return filename.substring(0, dot);
        }
    }

    public static OptionalInt parseInt(String str) {
        try {
            return OptionalInt.of(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static String formatDate(long date) {
        return formatDate(date, DDMMYYYY_HHMM);
    }

    public static String formatDate(long date, DateFormat format) {
        return format.format(Date.from(Instant.ofEpochMilli(date)));
    }

    public static String prettyDate(long millis) {
        return prettyDate(millis, false);
    }

    public static String prettyDate(long millis, boolean roundSecondsToInt) {
        if (millis < 1000) {
            return millis + " ms";
        } else if (millis < 1000 * 60) {
            double sec = millis / 1000d;

            return round(sec, roundSecondsToInt) + " s";
        } else if (millis < 1000 * 60 * 60) {
            int minute = (int) (millis / (1000 * 60d));
            double sec = (millis - minute * 1000 * 60) / 1000d;

            return minute + " min " + round(sec, roundSecondsToInt) + " s";
        } else {
            int hour = (int) (millis / (1000 * 60 * 60d));
            int minute = (int) (millis - hour * 1000 * 60 * 60) / (1000 * 60);
            double sec = (millis - hour * 1000 * 60 * 60 - minute * 1000 * 60) / 1000d;

            return hour + " h " + minute + " min " + round(sec, roundSecondsToInt) + " s";
        }
    }

    private static String round(double d, boolean roundToInt) {
        if (roundToInt) {
            return Integer.toString((int) d);
        } else {
            return FORMAT.format(d);
        }
    }

    public static Path checkExists(Path path) {
        if (Files.exists(path)) {
            String p = path.getFileName().toString();
            int dot = p.indexOf('.');

            String fileName;
            String extension;

            if (dot < 0) {
                fileName = p;
                extension = "";
            } else {
                fileName = p.substring(0, dot);
                extension = p.substring(dot); // keep dot
            }

            int i = 1;
            Path parent = path.getParent();
            Path newPath = resolve(parent, fileName + "_" + i + extension);
            while (Files.exists(newPath)) {
                i++;
                newPath = resolve(parent, fileName + "_" + i + extension);
            }

            return newPath;
        }

        return path;
    }

    private static Path resolve(Path parent, String s) {
        if (parent == null) {
            return Path.of(s);
        } else {
            return parent.resolve(s);
        }
    }
}
