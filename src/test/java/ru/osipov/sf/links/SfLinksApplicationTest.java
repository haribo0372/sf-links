package ru.osipov.sf.links;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SfLinksApplicationTest {

  @Test
  void classLoads() {
    assertThat(SfLinksApplication.class).isNotNull();
  }
}
