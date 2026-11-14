package ru.osipov.sf.links.exception;

public class LinkNotFoundException extends BaseException {

  public LinkNotFoundException(String code) {
    super(String.format("Ссылка не найдена по параметру: code='%s'", code));
  }
}
