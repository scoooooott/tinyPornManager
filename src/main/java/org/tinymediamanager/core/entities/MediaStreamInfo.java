package org.tinymediamanager.core.entities;

import java.util.EnumSet;

import org.tinymediamanager.core.AbstractModelObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MediaStreamInfo extends AbstractModelObject {

  /**
   * https://github.com/xbmc/xbmc/blob/master/xbmc/cores/VideoPlayer/Interface/StreamInfo.h
   */
  public enum Flags {
    // FLAG_NONE, // just empty
    FLAG_DEFAULT,
    FLAG_DUB,
    FLAG_ORIGINAL,
    FLAG_COMMENT,
    FLAG_LYRICS,
    FLAG_KARAOKE,
    FLAG_FORCED,
    FLAG_HEARING_IMPAIRED,
    FLAG_VISUAL_IMPAIRED
  }

  @JsonProperty
  public String         codec       = "";
  @JsonProperty
  public String         language    = "";
  @JsonProperty
  public EnumSet<Flags> streamFlags = EnumSet.noneOf(Flags.class);

  public String getCodec() {
    return codec;
  }

  public void setCodec(String codec) {
    this.codec = codec;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean has(Flags flag) {
    return streamFlags.contains(flag);
  }

  public void set(Flags... flags) {
    for (Flags f : flags) {
      streamFlags.add(f);
    }
  }

  public void remove(Flags... flags) {
    for (Flags f : flags) {
      streamFlags.remove(f);
    }
  }

  public boolean isDefaultStream() {
    return streamFlags.contains(Flags.FLAG_DEFAULT);
  }

  public void setDefaultStream(boolean defaultStream) {
    if (defaultStream) {
      streamFlags.add(Flags.FLAG_DEFAULT);
    }
    else {
      streamFlags.remove(Flags.FLAG_DEFAULT);
    }
  }

  public boolean isForced() {
    return streamFlags.contains(Flags.FLAG_FORCED);
  }

  public void setForced(boolean forced) {
    if (forced) {
      streamFlags.add(Flags.FLAG_FORCED);
    }
    else {
      streamFlags.remove(Flags.FLAG_FORCED);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((streamFlags == null) ? 0 : streamFlags.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MediaStreamInfo other = (MediaStreamInfo) obj;
    if (language == null) {
      if (other.language != null)
        return false;
    }
    else if (!language.equals(other.language))
      return false;
    if (streamFlags == null) {
      if (other.streamFlags != null)
        return false;
    }
    else if (!streamFlags.equals(other.streamFlags))
      return false;
    return true;
  }

}