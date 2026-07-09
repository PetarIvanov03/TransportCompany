package org.transport.ui;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// Shared console I/O helpers: typed input reading with re-prompt on invalid input, and uniform message formatting
public final class ConsolePrinter {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ConsolePrinter() {
    }

    public static void printError(String message) {
        System.out.println("[Грешка] " + message);
    }

    public static void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public static void printInfo(String message) {
        System.out.println(message);
    }

    public static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Returns null when the user leaves the field empty, so callers can distinguish "no change" from a value
    public static String readOptionalLine(Scanner scanner, String prompt) {
        System.out.print(prompt + " (Enter за без промяна/празно): ");
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? null : value;
    }

    public static Long readLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                printError("Моля, въведете цяло число.");
            }
        }
    }

    public static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                printError("Моля, въведете цяло число.");
            }
        }
    }

    public static BigDecimal readBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                printError("Моля, въведете валидно число (напр. 123.45).");
            }
        }
    }

    // Returns null when the user leaves the field empty (for optional numeric fields, e.g. cargoWeight)
    public static BigDecimal readOptionalBigDecimal(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (Enter за празно): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                printError("Моля, въведете валидно число или оставете празно.");
            }
        }
    }

    public static LocalDateTime readDateTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (формат yyyy-MM-dd HH:mm): ");
            String input = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(input, DATE_TIME_FORMAT);
            } catch (DateTimeParseException e) {
                printError("Невалиден формат на дата/час. Пример: 2026-07-10 14:30");
            }
        }
    }
}
