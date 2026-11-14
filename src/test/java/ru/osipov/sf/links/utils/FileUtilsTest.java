package ru.osipov.sf.links.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileUtilsTest {

  @TempDir Path tempDir;

  private ObjectMapper mapper;
  private Path file;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    file = tempDir.resolve("data.json");
  }

  @Test
  void save_thenLoad_restoresData() {
    Map<String, String> data = Map.of("a", "1", "b", "2");

    FileUtils.save(file.toString(), mapper, data);

    Map<String, String> loaded = new HashMap<>();
    FileUtils.load(file.toString(), mapper, new TypeReference<>() {}, loaded);

    assertThat(loaded).containsExactlyInAnyOrderEntriesOf(data);
  }

  @Test
  void save_createsFile() {
    FileUtils.save(file.toString(), mapper, Map.of("k", "v"));
    assertThat(Files.exists(file)).isTrue();
  }

  @Test
  void save_overwritesExistingFile() {
    FileUtils.save(file.toString(), mapper, Map.of("a", "1"));
    FileUtils.save(file.toString(), mapper, Map.of("b", "2"));

    Map<String, String> loaded = new HashMap<>();
    FileUtils.load(file.toString(), mapper, new TypeReference<>() {}, loaded);

    assertThat(loaded).containsOnlyKeys("b");
  }

  @Test
  void save_doesNotLeaveTmpFile() {
    FileUtils.save(file.toString(), mapper, Map.of("a", "1"));

    Path tmp = Path.of(file + ".tmp");
    assertThat(Files.exists(tmp)).isFalse(); // tmp переименован в целевой
  }

  @Test
  void load_missingFile_doesNothing() {
    Map<String, String> loaded = new HashMap<>();
    loaded.put("existing", "value");

    FileUtils.load(file.toString(), mapper, new TypeReference<>() {}, loaded);

    assertThat(loaded).containsExactlyEntriesOf(Map.of("existing", "value")); // не тронут
  }

  @Test
  void load_brokenJson_keepsExistingData() throws Exception {
    Files.writeString(file, "{ not valid json");

    Map<String, String> loaded = new HashMap<>();
    loaded.put("existing", "value");

    FileUtils.load(file.toString(), mapper, new TypeReference<>() {}, loaded);

    assertThat(loaded).containsExactlyEntriesOf(Map.of("existing", "value"));
  }

  @Test
  void load_clearsStorageBeforePutAll() {
    FileUtils.save(file.toString(), mapper, Map.of("new", "value"));

    Map<String, String> loaded = new HashMap<>();
    loaded.put("old", "stale");

    FileUtils.load(file.toString(), mapper, new TypeReference<>() {}, loaded);

    assertThat(loaded).containsOnlyKeys("new"); // "old" удалён
  }

  @Test
  void save_intoMissingDirectory_doesNotThrow() {
    String badPath = tempDir.resolve("no-such-dir").resolve("data.json").toString();

    FileUtils.save(badPath, mapper, Map.of("a", "1"));

    assertThat(Files.exists(Path.of(badPath))).isFalse();
  }
}
