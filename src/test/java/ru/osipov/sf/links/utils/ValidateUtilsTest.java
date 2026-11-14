package ru.osipov.sf.links.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValidateUtilsTest {

  @Test
  void isValidHttpsUrl_acceptsHttps() {
    assertThat(ValidateUtils.isValidHttpsUrl("https://example.com/path")).isTrue();
    assertThat(ValidateUtils.isValidHttpsUrl("https://example.com")).isTrue();
  }

  @Test
  void isValidHttpsUrl_rejectsNonHttps() {
    assertThat(ValidateUtils.isValidHttpsUrl("http://example.com")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("ftp://example.com")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("file:///etc/passwd")).isFalse();
  }

  @Test
  void isValidHttpsUrl_rejectsBroken() {
    assertThat(ValidateUtils.isValidHttpsUrl(null)).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("   ")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("https://")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("https:///path")).isFalse();
    assertThat(ValidateUtils.isValidHttpsUrl("not a url")).isFalse();
  }

  @Test
  void isValidShortLink_acceptsValid() {
    assertThat(ValidateUtils.isValidShortLink("clck.ru/3DZHeG")).isTrue();
    assertThat(ValidateUtils.isValidShortLink("clck.ru/abc")).isTrue();
    assertThat(ValidateUtils.isValidShortLink("clck.ru/A1b2C3")).isTrue();
  }

  @Test
  void isValidShortLink_rejectsInvalid() {
    assertThat(ValidateUtils.isValidShortLink(null)).isFalse();
    assertThat(ValidateUtils.isValidShortLink("")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("3DZHeG")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("https://clck.ru/3DZHeG")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("www.clck.ru/3DZHeG")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("hui.ru/3DZHeG")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("clck.ru/")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("clck.ru/abc/def")).isFalse();
    assertThat(ValidateUtils.isValidShortLink("clck.ru/abc?x=1")).isFalse();
  }
}
