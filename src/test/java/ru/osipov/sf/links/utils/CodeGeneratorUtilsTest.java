package ru.osipov.sf.links.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CodeGeneratorUtilsTest {

  private static final String ALPHABET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  @Test
  void generate_default_returns18Chars() {
    String code = CodeGeneratorUtils.generate();
    assertThat(code).hasSize(18);
    assertThat(code).matches("[" + ALPHABET + "]+");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 5, 18, 100})
  void generate_customLength(int length) {
    assertThat(CodeGeneratorUtils.generate(length)).hasSize(length);
  }

  @Test
  void generate_zero_returnsEmpty() {
    assertThat(CodeGeneratorUtils.generate(0)).isEmpty();
  }

  @Test
  void generate_negative_throws() {
    assertThatThrownBy(() -> CodeGeneratorUtils.generate(-1))
        .isInstanceOf(NegativeArraySizeException.class);
  }
}
