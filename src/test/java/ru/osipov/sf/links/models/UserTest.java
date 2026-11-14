package ru.osipov.sf.links.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

  @Test
  void builder_createsUser() {
    UUID uuid = UUID.randomUUID();

    User user = User.builder().uuid(uuid).username("alice").passwordHash("hash").build();

    assertThat(user.getUuid()).isEqualTo(uuid);
    assertThat(user.getUsername()).isEqualTo("alice");
    assertThat(user.getPasswordHash()).isEqualTo("hash");
  }

  @Test
  void noArgsConstructor_createsEmptyUser() {
    User user = new User();

    assertThat(user.getUuid()).isNull();
    assertThat(user.getUsername()).isNull();
    assertThat(user.getPasswordHash()).isNull();
  }

  @Test
  void allArgsConstructor_setsAllFields() {
    UUID uuid = UUID.randomUUID();

    User user = new User(uuid, "bob", "hash");

    assertThat(user.getUuid()).isEqualTo(uuid);
    assertThat(user.getUsername()).isEqualTo("bob");
    assertThat(user.getPasswordHash()).isEqualTo("hash");
  }

  @Test
  void settersAndGetters_work() {
    User user = new User();
    UUID uuid = UUID.randomUUID();

    user.setUuid(uuid);
    user.setUsername("carol");
    user.setPasswordHash("h");

    assertThat(user.getUuid()).isEqualTo(uuid);
    assertThat(user.getUsername()).isEqualTo("carol");
    assertThat(user.getPasswordHash()).isEqualTo("h");
  }

  @Test
  void equalsAndHashCode_byFields() {
    UUID uuid = UUID.randomUUID();

    User a = new User(uuid, "alice", "hash");
    User b = new User(uuid, "alice", "hash");

    assertThat(a).isEqualTo(b);
    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  void notEquals_whenFieldsDiffer() {
    UUID uuid = UUID.randomUUID();

    assertThat(new User(uuid, "alice", "hash")).isNotEqualTo(new User(uuid, "bob", "hash"));
  }

  @Test
  void toString_containsFields() {
    User user = new User(UUID.randomUUID(), "alice", "hash");

    assertThat(user.toString()).contains("alice", "hash");
  }
}
