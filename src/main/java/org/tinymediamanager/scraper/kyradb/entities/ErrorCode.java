package org.tinymediamanager.scraper.kyradb.entities;

public enum ErrorCode {

  UNKNOWN(-1),
  SUCCESSFUL(0),
  API_INVALID(1),
  API_BLOCKED(2),
  INVALID_URL(3),
  NO_RESULT(4);

  private final int code;

  private ErrorCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
