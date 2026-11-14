package ru.osipov.sf.links.exception;

public class LinkLimitExceededException extends BaseException {

  public LinkLimitExceededException(String code, Integer current, Integer limit) {
    super(
        String.format(
            "Превышен лимит переходов для короткой ссылки '%s': %d/%d", code, current, limit));
  }
}
