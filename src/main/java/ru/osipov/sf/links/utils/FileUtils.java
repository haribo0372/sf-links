package ru.osipov.sf.links.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class FileUtils {

  public static <T> void load(
      String fileName,
      ObjectMapper mapper,
      TypeReference<Map<String, T>> typeRef,
      Map<String, T> storage) {
    File f = new File(fileName);
    if (!f.exists()) return;
    try {
      Map<String, T> loaded = mapper.readValue(f, typeRef);
      storage.clear();
      storage.putAll(loaded);
    } catch (Exception e) {
      System.out.printf("Не удалось загрузить '%s': %s%n", fileName, e.getMessage());
    }
  }

  public static <T> void save(String fileName, ObjectMapper mapper, Map<String, T> storage) {
    try {
      Path tmp = Paths.get(fileName + ".tmp");
      mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), storage);
      Files.move(
          tmp,
          Paths.get(fileName),
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (Exception e) {
      System.out.printf("Не удалось сохранить '%s': %s%n", fileName, e.getMessage());
    }
  }
}
