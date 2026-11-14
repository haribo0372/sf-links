package ru.osipov.sf.links.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LinkTest {

  @Test
  void builder_createsLink() {
    UUID userId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    Link link =
        Link.builder()
            .url("https://example.com")
            .code("abc123")
            .maxRedirect(5)
            .currentRedirects(0)
            .userId(userId)
            .createdAt(now)
            .expiresAt(now.plusSeconds(60))
            .build();

    assertThat(link.getUrl()).isEqualTo("https://example.com");
    assertThat(link.getCode()).isEqualTo("abc123");
    assertThat(link.getMaxRedirect()).isEqualTo(5);
    assertThat(link.getCurrentRedirects()).isZero();
    assertThat(link.getUserId()).isEqualTo(userId);
    assertThat(link.getCreatedAt()).isEqualTo(now);
  }

  @Test
  void noArgsConstructor_createsEmptyLink() {
    Link link = new Link();

    assertThat(link.getUrl()).isNull();
    assertThat(link.getCode()).isNull();
    assertThat(link.getCurrentRedirects()).isNull();
    assertThat(link.getCreatedAt()).isNull();
  }

  @Test
  void allArgsConstructor_setsAllFields() {
    UUID userId = UUID.randomUUID();
    LocalDateTime created = LocalDateTime.now();
    LocalDateTime expires = created.plusSeconds(60);

    Link link = new Link("https://example.com", "abc123", 5, 2, userId, created, expires);

    assertThat(link.getUrl()).isEqualTo("https://example.com");
    assertThat(link.getCode()).isEqualTo("abc123");
    assertThat(link.getMaxRedirect()).isEqualTo(5);
    assertThat(link.getCurrentRedirects()).isEqualTo(2);
    assertThat(link.getUserId()).isEqualTo(userId);
    assertThat(link.getCreatedAt()).isEqualTo(created);
    assertThat(link.getExpiresAt()).isEqualTo(expires);
  }

  @Test
  void settersAndGetters_work() {
    Link link = new Link();

    link.setUrl("https://example.com");
    link.setCode("abc123");
    link.setMaxRedirect(5);
    link.setCurrentRedirects(1);
    link.setUserId(UUID.randomUUID());
    link.setCreatedAt(LocalDateTime.now());
    link.setExpiresAt(LocalDateTime.now());

    assertThat(link.getUrl()).isEqualTo("https://example.com");
    assertThat(link.getCode()).isEqualTo("abc123");
    assertThat(link.getMaxRedirect()).isEqualTo(5);
    assertThat(link.getCurrentRedirects()).isEqualTo(1);
  }

  @Test
  void equalsAndHashCode_byFields() {
    UUID userId = UUID.randomUUID();
    LocalDateTime created = LocalDateTime.now();
    LocalDateTime expires = created.plusSeconds(60);

    Link a = new Link("https://example.com", "abc123", 5, 0, userId, created, expires);
    Link b = new Link("https://example.com", "abc123", 5, 0, userId, created, expires);

    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  void notEquals_whenFieldsDiffer() {
    UUID userId = UUID.randomUUID();
    LocalDateTime created = LocalDateTime.now();

    Link a = new Link("https://a.com", "abc123", 5, 0, userId, created, created.plusSeconds(60));
    Link b = new Link("https://b.com", "abc123", 5, 0, userId, created, created.plusSeconds(60));

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void toString_containsFields() {
    Link link = new Link("https://example.com", "abc123", 5, UUID.randomUUID());

    assertThat(link.toString()).contains("https://example.com", "abc123");
  }
}
