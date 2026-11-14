package ru.osipov.sf.links.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.osipov.sf.links.config.ApplicationConfig;
import ru.osipov.sf.links.models.User;
import ru.osipov.sf.links.services.impl.AuthServiceImpl;

class AuthServiceImplTest {

  @TempDir Path tempDir;

  private ObjectMapper mapper;
  private String userFile;
  private AuthServiceImpl service;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    userFile = tempDir.resolve("users.json").toString();
    service = new AuthServiceImpl(config(userFile), mapper);
  }

  private ApplicationConfig config(String userFilePath) {
    Properties p = new Properties();
    p.setProperty("files.user", userFilePath);
    p.setProperty("files.link", tempDir.resolve("links.json").toString());
    p.setProperty("link.ttl", "60");
    return new ApplicationConfig(p);
  }

  @Test
  void register_success_returnsUserWithUuid() {
    Optional<User> result = service.register("alice", "secret");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("alice");
    assertThat(result.get().getUuid()).isNotNull();
    assertThat(result.get().getPasswordHash()).isNotEqualTo("secret");
  }

  @Test
  void register_blankUsername_returnsEmpty() {
    assertThat(service.register("", "secret")).isEmpty();
    assertThat(service.register("   ", "secret")).isEmpty();
    assertThat(service.register(null, "secret")).isEmpty();
  }

  @Test
  void register_blankPassword_returnsEmpty() {
    assertThat(service.register("alice", "")).isEmpty();
    assertThat(service.register("alice", "   ")).isEmpty();
    assertThat(service.register("alice", null)).isEmpty();
  }

  @Test
  void register_duplicateUsername_returnsEmpty() {
    service.register("alice", "secret");

    assertThat(service.register("alice", "other")).isEmpty();
  }

  @Test
  void login_success_returnsUser() {
    service.register("alice", "secret");

    Optional<User> result = service.login("alice", "secret");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("alice");
  }

  @Test
  void login_unknownUser_returnsEmpty() {
    assertThat(service.login("nobody", "secret")).isEmpty();
  }

  @Test
  void login_wrongPassword_returnsEmpty() {
    service.register("alice", "secret");

    assertThat(service.login("alice", "wrong")).isEmpty();
  }

  @Test
  void register_persistsUser_reloadFromFile() {
    service.register("alice", "secret");

    AuthServiceImpl reloaded = new AuthServiceImpl(config(userFile), mapper);

    assertThat(reloaded.login("alice", "secret")).isPresent();
  }
}
