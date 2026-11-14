package ru.osipov.sf.links.exception;

public class LinkAccessDeniedException extends BaseException {

  public LinkAccessDeniedException() {
    super("Доступ к ссылке запрещён");
  }
}
