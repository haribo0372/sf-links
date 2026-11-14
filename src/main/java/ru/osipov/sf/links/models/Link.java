package ru.osipov.sf.links.models;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Link {

  private String url;
  private String code;
  private Integer maxRedirect;
  private Integer currentRedirects;
  private UUID userId;
  private LocalDateTime createdAt;
  private LocalDateTime expiresAt;

  public Link(String url, String code, Integer maxRedirect, UUID userId) {
    this.url = url;
    this.code = code;
    this.maxRedirect = maxRedirect;
    this.userId = userId;

    this.currentRedirects = 0;
    this.createdAt = LocalDateTime.now();
  }
}
