package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaEpisode {
  public final String                  providerId;
  /** a hashmap storing id. */
  public final HashMap<String, Object> ids         = new HashMap<String, Object>();
  public int                           season      = -1;
  public int                           episode     = -1;
  public String                        title       = "";
  public String                        plot        = "";
  public double                        rating      = 0.0;
  public String                        firstAired  = "";

  public final List<MediaCastMember>   castMembers = new ArrayList<MediaCastMember>();
  public final List<MediaArtwork>      artwork     = new ArrayList<MediaArtwork>();

  public MediaEpisode(String providerId) {
    this.providerId = providerId;
  }
}
