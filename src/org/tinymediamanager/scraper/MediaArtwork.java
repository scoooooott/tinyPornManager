/*
 * Copyright 2012 - 2014 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class MediaArt.
 * 
 * @author Manuel Laggner
 */
public class MediaArtwork {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public enum MediaArtworkType {
    // @formatter:off
    BACKGROUND,
    BANNER,
    POSTER,
    ACTOR,
    SEASON,
    THUMB,
    CLEARART,
    DISC,
    LOGO,
    ALL;
    // @formatter:on

    /**
     * get the corresponding media file type for the artwork type
     * 
     * @param artworkType
     *          the artwork type
     * @return the media file type
     */
    public static MediaFileType getMediaFileType(MediaArtworkType artworkType) {
      switch (artworkType) {
        case BACKGROUND:
          return MediaFileType.FANART;

        case BANNER:
          return MediaFileType.BANNER;

        case POSTER:
          return MediaFileType.POSTER;

        case SEASON:
          return MediaFileType.SEASON_POSTER;

        case THUMB:
          return MediaFileType.THUMB;

        case CLEARART:
          return MediaFileType.CLEARART;

        case LOGO:
          return MediaFileType.LOGO;

        case DISC:
          return MediaFileType.DISCART;

        default:
          return MediaFileType.GRAPHIC;
      }
    }

    /**
     * get the corresponding artwork type for the file type
     * 
     * @param fileType
     *          the file type
     * @return the artwork type
     */
    public static MediaArtworkType getMediaArtworkType(MediaFileType fileType) {
      switch (fileType) {
        case FANART:
          return MediaArtworkType.BACKGROUND;

        case BANNER:
          return MediaArtworkType.BANNER;

        case POSTER:
          return MediaArtworkType.POSTER;

        case SEASON_POSTER:
          return MediaArtworkType.SEASON;

        case THUMB:
          return MediaArtworkType.THUMB;

        case CLEARART:
          return MediaArtworkType.CLEARART;

        case LOGO:
          return MediaArtworkType.LOGO;

        case DISCART:
          return MediaArtworkType.DISC;

        default:
          throw new IllegalStateException();
      }
    }
  }

  public enum PosterSizes {
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1000x1500px", 8), //$NON-NLS-1$
    BIG(BUNDLE.getString("Settings.image.big") + ": ~500x750px", 4), //$NON-NLS-1$
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~342x513px", 2), //$NON-NLS-1$
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~185x277px", 1); //$NON-NLS-1$

    private String text;
    private int    order;

    private PosterSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    @Override
    public String toString() {
      return this.text;
    }

    public int getOrder() {
      return this.order;
    }
  }

  public enum FanartSizes {
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1920x1080px", 8), //$NON-NLS-1$
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~1280x720px", 2), //$NON-NLS-1$
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~300x168px", 1); //$NON-NLS-1$

    private String text;
    private int    order;

    private FanartSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    @Override
    public String toString() {
      return this.text;
    }

    public int getOrder() {
      return order;
    }
  }

  private String                imdbId;
  private int                   tmdbId;
  private int                   season     = -1;
  private String                previewUrl = "";
  private String                defaultUrl = "";
  private String                language   = "";
  private String                providerId;
  private MediaArtworkType      type;
  private int                   sizeOrder  = 0;
  private int                   likes      = 0;

  private List<ImageSizeAndUrl> imageSizes = new ArrayList<ImageSizeAndUrl>();

  public MediaArtwork() {
  }

  public String getPreviewUrl() {
    return previewUrl;
  }

  public void setPreviewUrl(String downloadUrl) {
    this.previewUrl = downloadUrl;
  }

  public String getDefaultUrl() {
    return defaultUrl;
  }

  public void setDefaultUrl(String defaultUrl) {
    this.defaultUrl = defaultUrl;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public MediaArtworkType getType() {
    return type;
  }

  public void setType(MediaArtworkType type) {
    this.type = type;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    if (language == null) {
      this.language = "";
    }
    else {
      this.language = language;
    }
  }

  public int getTmdbId() {
    return tmdbId;
  }

  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public void addImageSize(int width, int height, String url) {
    imageSizes.add(new ImageSizeAndUrl(width, height, url));
  }

  public List<ImageSizeAndUrl> getImageSizes() {
    List<ImageSizeAndUrl> descImageSizes = new ArrayList<MediaArtwork.ImageSizeAndUrl>(imageSizes);

    // sort descending
    Collections.sort(descImageSizes, Collections.reverseOrder());

    return descImageSizes;
  }

  public ImageSizeAndUrl getSmallestArtwork() {
    if (imageSizes.size() > 0) {
      List<ImageSizeAndUrl> ascImageSizes = new ArrayList<MediaArtwork.ImageSizeAndUrl>(imageSizes);

      // sort ascending
      Collections.sort(ascImageSizes);
      ImageSizeAndUrl smallestImage = ascImageSizes.get(0);
      if (smallestImage != null) {
        return smallestImage;
      }
    }
    return null;
  }

  public ImageSizeAndUrl getBiggestArtwork() {
    if (imageSizes.size() > 0) {
      List<ImageSizeAndUrl> descImageSizes = new ArrayList<MediaArtwork.ImageSizeAndUrl>(imageSizes);

      // sort descending
      Collections.sort(descImageSizes, Collections.reverseOrder());
      ImageSizeAndUrl biggestImage = descImageSizes.get(0);
      if (biggestImage != null) {
        return biggestImage;
      }
    }
    return null;
  }

  public int getSizeOrder() {
    return sizeOrder;
  }

  public void setSizeOrder(int sizeOrder) {
    this.sizeOrder = sizeOrder;
  }

  public int getLikes() {
    return likes;
  }

  /**
   * amount of likes (or other, for ordering)
   * 
   * @param likes
   */
  public void setLikes(int likes) {
    this.likes = likes;
  }

  public int getSeason() {
    return season;
  }

  public void setSeason(int season) {
    this.season = season;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public static class ImageSizeAndUrl implements Comparable<ImageSizeAndUrl> {
    private int    width  = 0;
    private int    height = 0;
    private String url    = "";

    public ImageSizeAndUrl(int width, int height, String url) {
      this.width = width;
      this.height = height;
      this.url = url;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public String getUrl() {
      return url;
    }

    @Override
    public int compareTo(ImageSizeAndUrl obj) {
      return width - obj.width;
    }

    @Override
    public String toString() {
      return this.width + "x" + this.height;
    }
  }

  public static class MediaArtworkComparator implements Comparator<MediaArtwork> {
    private MediaLanguages preferredLangu = MediaLanguages.en;

    public MediaArtworkComparator(MediaLanguages language) {
      this.preferredLangu = language;
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(MediaArtwork arg0, MediaArtwork arg1) {
      String preferredLangu = this.preferredLangu.name();

      // check first if is preferred langu
      if (preferredLangu.equals(arg0.getLanguage()) && !preferredLangu.equals(arg1.getLanguage())) {
        return -1;
      }
      if (!preferredLangu.equals(arg0.getLanguage()) && preferredLangu.equals(arg1.getLanguage())) {
        return 1;
      }

      // not? compare with EN
      if ("en".equals(arg0.getLanguage()) && !"en".equals(arg1.getLanguage())) {
        return -1;
      }
      if (!"en".equals(arg0.getLanguage()) && "en".equals(arg1.getLanguage())) {
        return 1;
      }

      // we did not sort until here; so lets sort with the rating / likes
      if (arg0.getSizeOrder() == arg1.getSizeOrder()) {
        if (arg0.getLikes() == arg1.getLikes()) {
          return 0;
        }
        else {
          return arg0.getLikes() > arg1.getLikes() ? -1 : 1;
        }
      }
      else {
        return arg0.getSizeOrder() > arg1.getSizeOrder() ? -1 : 1;
      }
    }
  }
}
