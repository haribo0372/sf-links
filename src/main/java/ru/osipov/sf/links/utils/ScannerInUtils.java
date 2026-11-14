package ru.osipov.sf.links.utils;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;
import ru.osipov.sf.links.exception.ValidateInputException;

public final class ScannerInUtils {
  private static final String TEMPLATE_ERROR = "Ошибка ввода: %s.%n";
  private static Scanner scanner = new Scanner(System.in);

  private ScannerInUtils() {}

  public static <T> T read(String title, Function<String, T> parser) {
    while (true) {
      System.out.print(title);
      String line = scanner.nextLine().trim();
      try {
        return parser.apply(line);
      } catch (ValidateInputException e) {
        System.out.printf(TEMPLATE_ERROR, e.getMessage());
      }
    }
  }

  public static int readInteger(String title) {
    return read(
        title,
        s -> {
          try {
            return Integer.parseInt(s);
          } catch (NumberFormatException e) {
            throw new ValidateInputException("ожидается целое число");
          }
        });
  }

  public static String readString(String title, Predicate<String> predicate, String errorMessage) {
    return read(
        title,
        s -> {
          if (predicate.test(s)) return s;
          throw new ValidateInputException(errorMessage);
        });
  }
}
