package ru.osipov.sf.links.services;

import java.util.Optional;
import java.util.UUID;
import ru.osipov.sf.links.models.Link;

public interface LinkService {

  Link save(String url, Integer limitRedirect, UUID userId);

  void update(String code, String url, Integer limitRedirect, UUID userId);

  Optional<Link> findByCode(String code);

  void remove(String code, UUID userId);

  Optional<String> redirectByCode(String code, UUID userId);
}
