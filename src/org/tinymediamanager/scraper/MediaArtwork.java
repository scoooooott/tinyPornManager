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

  /**
   * The Enum MediaArtworkType.
   * 
   * @author Manuel Laggner
   */
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
          return MediaFileType.DISC;

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

        case DISC:
          return MediaArtworkType.DISC;

        default:
          throw new IllegalStateException();
      }
    }
  }

  /**
   * The Enum PosterSizes.
   * 
   * @author Manuel Laggner
   */
  public enum PosterSizes {
    /** The large. */
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1000x1500px", 8), //$NON-NLS-1$
    /** The big. */
    BIG(BUNDLE.getString("Settings.image.big") + ": ~500x750px", 4), //$NON-NLS-1$
    /** The medium. */
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~342x513px", 2), //$NON-NLS-1$
    /** The small. */
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~185x277px", 1); //$NON-NLS-1$

    /** The text. */
    private String text;

    /** The order. */
    private int    order;

    /**
     * Instantiates a new poster sizes.
     * 
     * @param text
     *          the text
     * @param order
     *          the order
     */
    private PosterSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    public String toString() {
      return this.text;
    }

    /**
     * Gets the order.
     * 
     * @return the order
     */
    public int getOrder() {
      return this.order;
    }
  }

  /**
   * The Enum FanartSizes.
   * 
   * @author Manuel Laggner
   */
  public enum FanartSizes {
    /** The large. */
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1920x1080px", 8), //$NON-NLS-1$
    /** The medium. */
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~1280x720px", 2), //$NON-NLS-1$
    /** The small. */
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~300x168px", 1); //$NON-NLS-1$

    /** The text. */
    private String text;

    /** The order. */
    private int    order;

    /**
     * Instantiates a new fanart sizes.
     * 
     * @param text
     *          the text
     * @param order
     *          the order
     */
    private FanartSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    public String toString() {
      return this.text;
    }

    /**
     * Gets the order.
     * 
     * @return the order
     */
    public int getOrder() {
      return order;
    }
  }

  private String                imdbId;
  private int                   tmdbId;
  private int                   season     = -1;
  private String                previewUrl;
  private String                defaultUrl;
  private String                language;
  private String                providerId;
  private MediaArtworkType      type;
  private int                   sizeOrder  = 0;

  private List<ImageSizeAndUrl> imageSizes = new ArrayList<ImageSizeAndUrl>();

  /**
   * Instantiates a new media art.
   */
  public MediaArtwork() {
  }

  /**
   * Gets the preview url.
   * 
   * @return the preview url
   */
  public String getPreviewUrl() {
    return previewUrl;
  }

  /**
   * Sets the preview url.
   * 
   * @param downloadUrl
   *          the new preview url
   */
  public void setPreviewUrl(String downloadUrl) {
    this.previewUrl = downloadUrl;
  }

  /**
   * Gets the default url.
   * 
   * @return the default url
   */
  public String getDefaultUrl() {
    return defaultUrl;
  }

  /**
   * Sets the default url.
   * 
   * @param defaultUrl
   *          the new default url
   */
  public void setDefaultUrl(String defaultUrl) {
    this.defaultUrl = defaultUrl;
  }

  /**
   * Gets the provider id.
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Sets the provider id.
   * 
   * @param providerId
   *          the new provider id
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public MediaArtworkType getType() {
    return type;
  }

  /**
   * Sets the type.
   * 
   * @param type
   *          the new type
   */
  public void setType(MediaArtworkType type) {
    this.type = type;
  }

  /**
   * Gets the language.
   * 
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets the language.
   * 
   * @param language
   *          the new language
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Gets the tmdb id.
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Sets the tmdb id.
   * 
   * @param tmdbId
   *          the new tmdb id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Gets the imdb id.
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Sets the imdb id.
   * 
   * @param imdbId
   *          the new imdb id
   */
  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  /**
   * Adds the image size.
   * 
   * @param width
   *          the width
   * @param height
   *          the height
   * @param url
   *          the url
   */
  public void addImageSize(int width, int height, String url) {
    imageSizes.add(new ImageSizeAndUrl(width, height, url));
  }

  /**
   * Gets the image sizes.
   * 
   * @return the image sizes
   */
  public List<ImageSizeAndUrl> getImageSizes() {
    List<ImageSizeAndUrl> descImageSizes = new ArrayList<MediaArtwork.ImageSizeAndUrl>(imageSizes);

    // sort descending
    Collections.sort(descImageSizes, Collections.reverseOrder());

    return descImageSizes;
  }

  /**
   * gets the Url for the smallest available artwork (most used for previews - imagechooser).
   * 
   * @return the url for small artwork
   */
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

  /**
   * gets the Url for the biggest available artwork (most used for real downloads).
   * 
   * @return the url for biggest artwork
   */
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

  /**
   * Gets the size order.
   * 
   * @return the size order
   */
  public int getSizeOrder() {
    return sizeOrder;
  }

  /**
   * Sets the size order.
   * 
   * @param sizeOrder
   *          the new size order
   */
  public void setSizeOrder(int sizeOrder) {
    this.sizeOrder = sizeOrder;
  }

  /**
   * Gets the season.
   * 
   * @return the season
   */
  public int getSeason() {
    return season;
  }

  /**
   * Sets the season.
   * 
   * @param season
   *          the new season
   */
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

  /**
   * The Class ImageSizeAndUrl.
   * 
   * @author Manuel Laggner
   */
  public static class ImageSizeAndUrl implements Comparable<ImageSizeAndUrl> {

    /** The width. */
    private int    width  = 0;

    /** The height. */
    private int    height = 0;

    /** The url. */
    private String url    = "";

    /**
     * Instantiates a new image size and url.
     * 
     * @param width
     *          the width
     * @param height
     *          the height
     * @param url
     *          the url
     */
    public ImageSizeAndUrl(int width, int height, String url) {
      this.width = width;
      this.height = height;
      this.url = url;
    }

    /**
     * Gets the width.
     * 
     * @return the width
     */
    public int getWidth() {
      return width;
    }

    /**
     * Gets the height.
     * 
     * @return the height
     */
    public int getHeight() {
      return height;
    }

    /**
     * Gets the url.
     * 
     * @return the url
     */
    public String getUrl() {
      return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ImageSizeAndUrl obj) {
      return width - obj.width;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return this.width + "x" + this.height;
    }
  }
}
