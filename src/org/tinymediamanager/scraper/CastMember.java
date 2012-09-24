package org.tinymediamanager.scraper;

import java.util.LinkedList;
import java.util.List;

public class CastMember {
  private String          id;
  private String          name;
  private String          part;
  private String          character;

  private String          providerDataUrl;
  private int             type;
  private List<String>    fanart   = new LinkedList<String>();

  public static final int ACTOR    = 0;
  public static final int WRITER   = 1;
  public static final int DIRECTOR = 2;
  public static final int OTHER    = 99;
  public static final int ALL      = 999;

  public CastMember() {
  }

  public CastMember(int type) {
    setType(type);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPart() {
    return part;
  }

  public void setPart(String part) {
    this.part = part;
  }

  public String getProviderDataUrl() {
    return providerDataUrl;
  }

  public void setProviderDataUrl(String providerDataUrl) {
    this.providerDataUrl = providerDataUrl;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public void addFanart(String url) {
    if (url != null) {
      fanart.add(url.trim());
    }
  }

  public String getCharacter() {
    return character;
  }

  public void setCharacter(String character) {
    this.character = character;
  }

}
