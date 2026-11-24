package ru.osipov.sf.links;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Scanner;
import ru.osipov.sf.links.config.ApplicationConfig;
import ru.osipov.sf.links.config.JsonConfig;
import ru.osipov.sf.links.models.User;
import ru.osipov.sf.links.services.AuthService;
import ru.osipov.sf.links.services.LinkService;
import ru.osipov.sf.links.services.impl.AuthServiceImpl;
import ru.osipov.sf.links.services.impl.LinkServiceImpl;
import ru.osipov.sf.links.utils.ScannerInUtils;
import ru.osipov.sf.links.utils.ValidateUtils;

public class SfLinksApplication {

  private static final String SHORT_LINK_DOMAIN = "clck.ru";

  private static final Scanner scanner;
  private static final ApplicationConfig applicationConfig;
  private static final JsonConfig jsonConfig;
  private static final AuthService authService;
  private static final LinkService linkService;
  private static User currentUser = null;

  static {
    scanner = new Scanner(System.in);

    applicationConfig = new ApplicationConfig();
    jsonConfig = new JsonConfig();

    authService = new AuthServiceImpl(applicationConfig, jsonConfig.getMapper());
    linkService = new LinkServiceImpl(applicationConfig, jsonConfig.getMapper());
  }

  public static void main(String[] args) {
    while (true) {
      if (currentUser == null) {
        guestMenu();
      } else {
        userMenu();
      }
    }
  }

  private static void guestMenu() {
    System.out.println("\n------------------------------------------------------");
    System.out.println("\t=== ГЛАВНОЕ МЕНЮ ===");
    System.out.println("------------------------------------------------------");
    System.out.println("1. Войти");
    System.out.println("2. Зарегистрироваться");
    System.out.println("0. Выйти из программы");
    System.out.print("Выбор: ");
    String ch = scanner.nextLine().trim();

    switch (ch) {
      case "1" -> doLogin();
      case "2" -> doRegister();
      case "0" -> {
        System.out.println("До свидания!");
        System.exit(0);
      }
      default -> System.out.println("Неверный выбор.");
    }
  }

  private static void userMenu() {
    System.out.println("\n------------------------------------------------------");
    System.out.println("\t=== МЕНЮ (" + currentUser.getUsername() + ") ===");
    System.out.println("------------------------------------------------------");
    System.out.println("1. Выйти из профиля");
    System.out.println("2. Создать короткую ссылку");
    System.out.println("3. Перейти по короткой ссылке");
    System.out.println("4. Редактировать короткую ссылку");
    System.out.println("5. Удалить короткую ссылку");
    System.out.println("0. Выйти из программы");
    System.out.print("Выбор: ");
    String ch = scanner.nextLine().trim();

    switch (ch) {
      case "1" -> {
        System.out.println("Вы вышли из профиля.");
        currentUser = null;
      }
      case "2" -> doCreateNewLink();
      case "3" -> doRedirect();
      case "4" -> doUpdate();
      case "5" -> doRemove();
      case "0" -> {
        System.out.println("До свидания!");
        System.exit(0);
      }
      default -> System.out.println("Неверный выбор.");
    }
  }

  private static void doLogin() {
    String login =
        ScannerInUtils.readString("Логин: ", s -> !s.isBlank(), "строка не может быть пустой");
    String password =
        ScannerInUtils.readString("Пароль: ", s -> !s.isBlank(), "строка не может быть пустой");

    Optional<User> user = authService.login(login, password);
    user.ifPresent(u -> currentUser = u);
  }

  private static void doRegister() {
    String login =
        ScannerInUtils.readString("Логин: ", s -> !s.isBlank(), "строка не может быть пустой");
    String password =
        ScannerInUtils.readString("Пароль: ", s -> !s.isBlank(), "строка не может быть пустой");

    Optional<User> user = authService.register(login, password);
    user.ifPresent(u -> currentUser = u);
  }

  private static void doCreateNewLink() {
    String url = readHttpsFromConsole("Введите ссылку (https): ");
    Integer limit = ScannerInUtils.readInteger("Укажите лимит переходов: ");
    linkService.save(url, limit, currentUser.getUuid());
  }

  private static void doRedirect() {
    linkService
        .redirectByCode(readCodeFromConsole(), currentUser.getUuid())
        .ifPresent(
            url -> {
              if (!tryOpenInBrowser(url)) {
                System.out.printf("Открыть браузер не удалось. Ссылка: %s%n", url);
              } else {
                System.out.printf("Осуществлен переход по ссылке: %s%n", url);
              }
            });
  }

  private static void doUpdate() {
    String code = readCodeFromConsole();
    String url = readHttpsFromConsole("Введите новую ссылку (https): ");
    Integer limit = ScannerInUtils.readInteger("Укажите новый лимит переходов: ");
    linkService.update(code, url, limit, currentUser.getUuid());
  }

  private static void doRemove() {
    linkService.remove(readCodeFromConsole(), currentUser.getUuid());
  }

  private static String readCodeFromConsole() {
    String input =
        ScannerInUtils.readString(
            "Введите короткую ссылку (например 'clck.ru/3DZHeG'): ",
            ValidateUtils::isValidShortLink,
            "Неверный формат. Ожидается clck.ru/<код>, например: clck.ru/3DZHeG");

    return input.substring((SHORT_LINK_DOMAIN + "/").length());
  }

  private static String readHttpsFromConsole(String title) {
    return ScannerInUtils.readString(
        title,
        ValidateUtils::isValidHttpsUrl,
        "Некорректная ссылка. Поддерживается только https, например: https://example.com/path");
  }

  private static boolean tryOpenInBrowser(String url) {
    if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
      return false;
    }
    try {
      Desktop.getDesktop().browse(new URI(url));
      return true;
    } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
      System.out.println("Не удалось открыть браузер: " + e.getMessage());
      return false;
    }
  }
}
