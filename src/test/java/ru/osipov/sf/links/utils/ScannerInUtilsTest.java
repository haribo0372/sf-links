package ru.osipov.sf.links.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Scanner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.osipov.sf.links.exception.ValidateInputException;

class ScannerInUtilsTest {

  private PrintStream originalOut;
  private ByteArrayOutputStream out;

  @BeforeEach
  void setUp() throws Exception {
    originalOut = System.out;
    out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
  }

  @AfterEach
  void tearDown() throws Exception {
    System.setOut(originalOut);
    setScanner(new Scanner(System.in));
  }

  private static void setScanner(Scanner newScanner) throws Exception {
    Field field = ScannerInUtils.class.getDeclaredField("scanner");
    field.setAccessible(true);
    field.set(null, newScanner);
  }

  private static void feedInput(String input) throws Exception {
    setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "1", "42", "-7", "1000"})
  void readInteger_validInput_returnsInt(String input) throws Exception {
    feedInput(input + "\n");
    assertThat(ScannerInUtils.readInteger("Введите: ")).isEqualTo(Integer.parseInt(input));
  }

  @Test
  void readInteger_invalidThenValid_retries() throws Exception {
    feedInput("abc\n42\n");
    assertThat(ScannerInUtils.readInteger("Введите: ")).isEqualTo(42);
    assertThat(out.toString()).contains("ожидается целое число");
  }

  @Test
  void readString_valid_returnsTrimmed() throws Exception {
    feedInput("  hello  \n");
    String result = ScannerInUtils.readString("Введите: ", s -> !s.isBlank(), "пусто");
    assertThat(result).isEqualTo("hello");
  }

  @Test
  void readString_invalidThenValid_retries() throws Exception {
    feedInput("\n\nhello\n");
    String result = ScannerInUtils.readString("Введите: ", s -> !s.isBlank(), "пусто");
    assertThat(result).isEqualTo("hello");
    assertThat(out.toString()).contains("пусто");
  }

  @Test
  void read_parserThrowsThenSucceeds_retries() throws Exception {
    feedInput("bad\ngood\n");
    String result =
        ScannerInUtils.read(
            "Введите: ",
            s -> {
              if (s.equals("good")) return s;
              throw new ValidateInputException("не good");
            });
    assertThat(result).isEqualTo("good");
    assertThat(out.toString()).contains("не good");
  }

  @Test
  void constructor_isPrivate() throws Exception {
    var ctor = ScannerInUtils.class.getDeclaredConstructor();
    assertThat(java.lang.reflect.Modifier.isPrivate(ctor.getModifiers())).isTrue();
  }
}
