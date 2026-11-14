package ru.osipov.sf.links.services.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.links.config.ApplicationConfig;
import ru.osipov.sf.links.exception.BaseException;
import ru.osipov.sf.links.exception.LinkAccessDeniedException;
import ru.osipov.sf.links.exception.LinkLimitExceededException;
import ru.osipov.sf.links.exception.LinkNotFoundException;
import ru.osipov.sf.links.models.Link;
import ru.osipov.sf.links.services.LinkService;
import ru.osipov.sf.links.utils.CodeGeneratorUtils;
import ru.osipov.sf.links.utils.FileUtils;

public class LinkServiceImpl implements LinkService {

  private final String linkFile;
  private final Long ttl;
  private final ObjectMapper mapper;
  private final Map<String, Link> links;

  public LinkServiceImpl(ApplicationConfig applicationConfig, ObjectMapper mapper) {
    this.linkFile = applicationConfig.getLinkFile();
    this.ttl = applicationConfig.getLinkTtlSeconds();
    this.links = new HashMap<>();
    this.mapper = mapper;
    FileUtils.load(this.linkFile, this.mapper, new TypeReference<Map<String, Link>>() {}, links);
  }

  @Override
  public Link save(String url, Integer limitRedirect, UUID userId) {
    if (url == null || url.isBlank()) {
      System.out.println("Ссылка не может быть пустой.");
      return null;
    }

    if (limitRedirect <= 0) {
      System.out.println("Лимит переходов должен быть положительным числом.");
      return null;
    }

    String uniqueCode = generateUniqueCode();
    var link = new Link(url, uniqueCode, limitRedirect, userId);
    link.setExpiresAt(link.getCreatedAt().plusSeconds(ttl));
    links.put(uniqueCode, link);
    FileUtils.save(linkFile, mapper, links);
    System.out.println("Ссылка успешно создана. Ссылка: clck.ru/" + uniqueCode);
    return link;
  }

  @Override
  public void update(String code, String url, UUID userId) {
    try {
      Link link = this.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
      checkAccessByUserId(link, userId);
      link.setUrl(url);
      FileUtils.save(linkFile, mapper, links);
      System.out.printf("Ссылка с code='%s' успешно обновлена%n", code);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  @Override
  public Optional<String> redirectByCode(String code, UUID userId) {
    try {
      var link = this.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
      //      checkAccessByUserId(link, userId);
      if (checkLimitExpired(link)) return Optional.empty();
      checkLimitExceeded(link);

      link.setCurrentRedirects(link.getCurrentRedirects() + 1);
      FileUtils.save(linkFile, mapper, links);

      if (link.getCurrentRedirects() >= link.getMaxRedirect()) {
        System.out.println("Внимание: это последний переход по ссылке.");
      }
      return Optional.of(link.getUrl());
    } catch (BaseException e) {
      System.out.println(e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public Optional<Link> findByCode(String code) {
    return Optional.ofNullable(links.get(code));
  }

  @Override
  public void remove(String code, UUID userId) {
    try {
      Link link = this.findByCode(code).orElseThrow(() -> new LinkNotFoundException(code));
      checkAccessByUserId(link, userId);
      this.remove(code);
    } catch (BaseException e) {
      System.out.println(e.getMessage());
    }
  }

  private void remove(String code) {
    links.remove(code);
    FileUtils.save(linkFile, mapper, links);
    System.out.printf("Ссылка с code='%s' удалена%n", code);
  }

  private String generateUniqueCode() {
    String code;
    do {
      code = CodeGeneratorUtils.generate();
    } while (links.containsKey(code));
    return code;
  }

  private void checkAccessByUserId(Link link, UUID userId) {
    if (!link.getUserId().equals(userId)) {
      throw new LinkAccessDeniedException();
    }
  }

  private void checkLimitExceeded(Link link) {
    if (link.getMaxRedirect() <= link.getCurrentRedirects()) {
      throw new LinkLimitExceededException(
          link.getCode(), link.getCurrentRedirects(), link.getMaxRedirect());
    }
  }

  private boolean checkLimitExpired(Link link) {
    if (LocalDateTime.now().isAfter(link.getExpiresAt())) {
      System.out.printf(
          "Срок действия ссылки истек. Ссылка с code='%s' будет удалена%n", link.getCode());
      this.remove(link.getCode());
      return true;
    }
    return false;
  }
}
