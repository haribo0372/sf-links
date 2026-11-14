package ru.osipov.sf.links.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.osipov.sf.links.config.ApplicationConfig;
import ru.osipov.sf.links.models.Link;
import ru.osipov.sf.links.services.impl.LinkServiceImpl;

class LinkServiceImplTest {

  @TempDir Path tempDir;

  private ObjectMapper mapper;
  private ApplicationConfig config;
  private LinkServiceImpl service;
  private UUID userId;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    config = config(tempDir.resolve("links.json").toString(), "60");
    service = new LinkServiceImpl(config, mapper);
    userId = UUID.randomUUID();
  }

  private ApplicationConfig config(String linkFile, String ttl) {
    Properties p = new Properties();
    p.setProperty("files.user", tempDir.resolve("users.json").toString());
    p.setProperty("files.link", linkFile);
    p.setProperty("link.ttl", ttl);
    return new ApplicationConfig(p);
  }

  @Test
  void save_valid_returnsLinkWithUniqueCode() {
    Link link = service.save("https://example.com", 5, userId);

    assertThat(link).isNotNull();
    assertThat(link.getCode()).isNotBlank();
    assertThat(link.getUrl()).isEqualTo("https://example.com");
    assertThat(link.getMaxRedirect()).isEqualTo(5);
    assertThat(link.getCurrentRedirects()).isZero();
    assertThat(link.getUserId()).isEqualTo(userId);
    assertThat(service.findByCode(link.getCode())).isPresent();
  }

  @Test
  void save_setsExpiresAtFromTtl() {
    Link link = service.save("https://example.com", 5, userId);

    assertThat(link.getExpiresAt()).isEqualTo(link.getCreatedAt().plusSeconds(60));
  }

  @Test
  void save_customTtl_applied() {
    LinkServiceImpl custom =
        new LinkServiceImpl(config(tempDir.resolve("links2.json").toString(), "3600"), mapper);

    Link link = custom.save("https://example.com", 5, userId);

    assertThat(link.getExpiresAt()).isEqualTo(link.getCreatedAt().plusSeconds(3600));
  }

  @Test
  void save_sameUrlDifferentUsers_differentCodes() {
    UUID userB = UUID.randomUUID();

    Link a = service.save("https://example.com", 5, userId);
    Link b = service.save("https://example.com", 5, userB);

    assertThat(a.getCode()).isNotEqualTo(b.getCode());
  }

  @Test
  void save_nullUrl_returnsNull() {
    assertThat(service.save(null, 5, userId)).isNull();
  }

  @Test
  void save_blankUrl_returnsNull() {
    assertThat(service.save("   ", 5, userId)).isNull();
  }

  @Test
  void save_nonPositiveLimit_returnsNull() {
    assertThat(service.save("https://example.com", 0, userId)).isNull();
    assertThat(service.save("https://example.com", -1, userId)).isNull();
  }

  @Test
  void save_persistsLink_reloadFromFile() {
    Link link = service.save("https://example.com", 5, userId);

    LinkServiceImpl reloaded =
        new LinkServiceImpl(config(tempDir.resolve("links.json").toString(), "60"), mapper);

    assertThat(reloaded.findByCode(link.getCode())).isPresent();
    assertThat(reloaded.findByCode(link.getCode()).orElseThrow().getUrl())
        .isEqualTo("https://example.com");
  }

  @Test
  void update_owner_changesUrlAndLimit() {
    Link link = service.save("https://old.com", 5, userId);

    service.update(link.getCode(), "https://new.com", 10, userId);

    Link updated = service.findByCode(link.getCode()).orElseThrow();
    assertThat(updated.getUrl()).isEqualTo("https://new.com");
    assertThat(updated.getMaxRedirect()).isEqualTo(10);
  }

  @Test
  void update_notOwner_keepsLinkUnchanged() {
    Link link = service.save("https://old.com", 5, userId);
    UUID stranger = UUID.randomUUID();

    service.update(link.getCode(), "https://new.com", 10, stranger);

    Link unchanged = service.findByCode(link.getCode()).orElseThrow();
    assertThat(unchanged.getUrl()).isEqualTo("https://old.com");
    assertThat(unchanged.getMaxRedirect()).isEqualTo(5);
  }

  @Test
  void update_unknownCode_doesNothing() {
    service.update("missing", "https://new.com", 10, userId);

    assertThat(service.findByCode("missing")).isEmpty();
  }

  @Test
  void update_persistsChanges_reloadFromFile() {
    Link link = service.save("https://old.com", 5, userId);
    service.update(link.getCode(), "https://new.com", 10, userId);

    LinkServiceImpl reloaded =
        new LinkServiceImpl(config(tempDir.resolve("links.json").toString(), "60"), mapper);

    Link loaded = reloaded.findByCode(link.getCode()).orElseThrow();
    assertThat(loaded.getUrl()).isEqualTo("https://new.com");
    assertThat(loaded.getMaxRedirect()).isEqualTo(10);
  }

  @Test
  void redirect_valid_returnsUrlAndIncrements() {
    Link link = service.save("https://example.com", 3, userId);

    Optional<String> result = service.redirectByCode(link.getCode(), userId);

    assertThat(result).contains("https://example.com");
    assertThat(service.findByCode(link.getCode()).orElseThrow().getCurrentRedirects()).isEqualTo(1);
  }

  @Test
  void redirect_lastRedirect_succeeds() {
    Link link = service.save("https://example.com", 1, userId);

    Optional<String> result = service.redirectByCode(link.getCode(), userId);

    assertThat(result).contains("https://example.com");
    assertThat(service.findByCode(link.getCode()).orElseThrow().getCurrentRedirects()).isEqualTo(1);
  }

  @Test
  void redirect_limitExceeded_returnsEmpty() {
    Link link = service.save("https://example.com", 1, userId);
    service.redirectByCode(link.getCode(), userId);

    assertThat(service.redirectByCode(link.getCode(), userId)).isEmpty();
  }

  @Test
  void redirect_unknownCode_returnsEmpty() {
    assertThat(service.redirectByCode("nope123", userId)).isEmpty();
  }

  @Test
  void redirect_expiredLink_removesAndReturnsEmpty() {
    Link link = service.save("https://example.com", 5, userId);
    link.setExpiresAt(LocalDateTime.now().minusDays(1));

    Optional<String> result = service.redirectByCode(link.getCode(), userId);

    assertThat(result).isEmpty();
    assertThat(service.findByCode(link.getCode())).isEmpty();
  }

  @Test
  void findByCode_existing_returnsLink() {
    Link link = service.save("https://example.com", 5, userId);

    assertThat(service.findByCode(link.getCode())).contains(link);
  }

  @Test
  void findByCode_missing_returnsEmpty() {
    assertThat(service.findByCode("missing")).isEmpty();
  }

  @Test
  void remove_owner_removesLink() {
    Link link = service.save("https://example.com", 5, userId);

    service.remove(link.getCode(), userId);

    assertThat(service.findByCode(link.getCode())).isEmpty();
  }

  @Test
  void remove_notOwner_keepsLink() {
    Link link = service.save("https://example.com", 5, userId);
    UUID stranger = UUID.randomUUID();

    service.remove(link.getCode(), stranger);

    assertThat(service.findByCode(link.getCode())).isPresent();
  }

  @Test
  void remove_unknownCode_doesNothing() {
    service.remove("missing", userId);

    assertThat(service.findByCode("missing")).isEmpty();
  }

  @Test
  void remove_persistsDeletion_reloadFromFile() {
    Link link = service.save("https://example.com", 5, userId);
    service.remove(link.getCode(), userId);

    LinkServiceImpl reloaded =
        new LinkServiceImpl(config(tempDir.resolve("links.json").toString(), "60"), mapper);

    assertThat(reloaded.findByCode(link.getCode())).isEmpty();
  }
}
