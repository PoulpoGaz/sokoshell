package fr.valax.sokoshell.utils;

import java.util.Locale;
import java.util.Scanner;

public class ScanUtils {

    public static final int NO_DEFAULT = 0;
    public static final int DEFAULT_YES = 1;
    public static final int DEFAULT_NO = 2;

    public static boolean yesNoQuestion(String question, int defaultValue) {
        Scanner sc = new Scanner(System.in);

        System.out.print(question);

        switch (defaultValue) {
            case DEFAULT_YES -> System.out.print(" [Y/n]:");
            case DEFAULT_NO -> System.out.print(" [y/N]:");
            default -> {
                System.out.print(" [y/n]:");
                defaultValue = NO_DEFAULT;
            }
        }

        while (true) {
            if (sc.hasNextLine()) {
                String line = sc.nextLine().toLowerCase(Locale.ROOT);

                if (line.isBlank() && defaultValue != NO_DEFAULT) {
                    return defaultValue == DEFAULT_YES;
                } else if (line.equals("y")) {
                    return true;
                } else if (line.equals("n")) {
                    return false;
                } else {
                    System.out.println(question + " [y/N]:");
                }
            }
        }
    }
}
