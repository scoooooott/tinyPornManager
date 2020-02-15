/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.scraper.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * This class is used to represent an artwork for a media
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class MediaArtwork {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  /**
   * The different types of artwork we know
   * 
   * @author Manuel Laggner
   * @since 1.0
   */
  public enum MediaArtworkType {
    // @formatter:off
    BACKGROUND, 
    BANNER, 
    POSTER, 
    ACTOR,
    SEASON_POSTER,
    SEASON_BANNER,
    SEASON_THUMB,
    THUMB, 
    CLEARART,
    KEYART,
    CHARACTERART,
    DISC, 
    LOGO,
    CLEARLOGO,
    ALL
    // @formatter:on
  }

  /**
   * All available poster sizes
   * 
   * @author Manuel Laggner
   * @since 1.0
   */
  public enum PosterSizes {
    XLARGE(BUNDLE.getString("Settings.image.xlarge") + ": ~2000x3000px", 16),
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1000x1500px", 8),
    BIG(BUNDLE.getString("Settings.image.big") + ": ~500x750px", 4),
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~342x513px", 2),
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~185x277px", 1);

    private String text;
    private int    order;

    PosterSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    @Override
    public String toString() {
      return text;
    }

    public int getOrder() {
      return order;
    }
  }

  /**
   * All available fanart sizes
   * 
   * @author Manuel Laggner
   * @since 1.0
   */
  public enum FanartSizes {
    XLARGE(BUNDLE.getString("Settings.image.xlarge") + ": ~3840x2160px", 16),
    LARGE(BUNDLE.getString("Settings.image.large") + ": ~1920x1080px", 8),
    MEDIUM(BUNDLE.getString("Settings.image.medium") + ": ~1280x720px", 2),
    SMALL(BUNDLE.getString("Settings.image.small") + ": ~300x168px", 1);

    private String text;
    private int    order;

    FanartSizes(String text, int order) {
      this.text = text;
      this.order = order;
    }

    @Override
    public String toString() {
      return text;
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
  private boolean               animated   = false;

  private List<ImageSizeAndUrl> imageSizes = new ArrayList<>();

  /**
   * Create a new instance of MediaArtwork for the given provider and type
   * 
   * @param providerId
   *          the provider id
   * @param type
   *          the artwork type
   */
  public MediaArtwork(String providerId, MediaArtworkType type) {
    this.providerId = providerId;
    this.type = type;
  }

  /**
   * A copy constructor for MediaArtwork - to clone an existing one for a new type
   * 
   * @param oldArtwork
   *          the instance to copy
   * @param type
   *          the new type
   */
  public MediaArtwork(MediaArtwork oldArtwork, MediaArtworkType type) {
    this.imdbId = oldArtwork.getImdbId();
    this.tmdbId = oldArtwork.getTmdbId();
    this.season = oldArtwork.getSeason();
    this.previewUrl = oldArtwork.getPreviewUrl();
    this.defaultUrl = oldArtwork.getDefaultUrl();
    this.language = oldArtwork.getLanguage();
    this.providerId = oldArtwork.getProviderId();
    this.sizeOrder = oldArtwork.getSizeOrder();
    this.likes = oldArtwork.getLikes();

    for (ImageSizeAndUrl oldImageSizeAndUrl : oldArtwork.getImageSizes()) {
      this.imageSizes.add(new ImageSizeAndUrl(oldImageSizeAndUrl.width, oldImageSizeAndUrl.height, oldImageSizeAndUrl.url));
    }

    this.type = type;
  }

  /**
   * Get a preview url or the default url if no preview url is available
   * 
   * @return the preview url or the default url
   */
  public String getPreviewUrl() {
    // return the default url if the preview url has not been set
    if (StringUtils.isBlank(previewUrl)) {
      return defaultUrl;
    }
    return previewUrl;
  }

  /**
   * Set the preview url
   * 
   * @param previewUrl
   *          the preview url
   */
  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  /**
   * Get the default url for this artwork
   * 
   * @return the default url
   */
  public String getDefaultUrl() {
    return defaultUrl;
  }

  /**
   * Set the default url
   * 
   * @param defaultUrl
   *          the default url
   */
  public void setDefaultUrl(String defaultUrl) {
    this.defaultUrl = defaultUrl;
  }

  /**
   * Get the provider id
   * 
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Get the artwork type
   * 
   * @return the artwork type
   */
  public MediaArtworkType getType() {
    return type;
  }

  /**
   * Get the language for this artwork if available
   * 
   * @return the language for this artwork
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set the language for this artwork
   * 
   * @param language
   *          the language
   */
  public void setLanguage(String language) {
    this.language = StrgUtils.getNonNullString(language);
  }

  /**
   * Get the assigned TMDB id for this artwork
   * 
   * @return the tmdb id
   */
  public int getTmdbId() {
    return tmdbId;
  }

  /**
   * Set the assigned TMDB id for this artwork
   * 
   * @param tmdbId
   *          the tmdb id
   */
  public void setTmdbId(int tmdbId) {
    this.tmdbId = tmdbId;
  }

  /**
   * Get the assigned IMDB id for this artwork
   * 
   * @return the imdb id
   */
  public String getImdbId() {
    return imdbId;
  }

  /**
   * Set the assigned IMDB id for this artwork
   * 
   * @param imdbId
   *          the imdb id
   */
  public void setImdbId(String imdbId) {
    this.imdbId = StrgUtils.getNonNullString(imdbId);
  }

  /**
   * Add an image size. This is used for the image chooser to show the user the different sizes for this artwork
   * 
   * @param width
   *          thw width
   * @param height
   *          the height
   * @param url
   *          the url
   */
  public void addImageSize(int width, int height, String url) {
    imageSizes.add(new ImageSizeAndUrl(width, height, url));
  }

  /**
   * Get all available image sizes for this artwork
   * 
   * @return a list of all available image sizes
   */
  public List<ImageSizeAndUrl> getImageSizes() {
    List<ImageSizeAndUrl> descImageSizes = new ArrayList<>(imageSizes);

    // sort descending
    descImageSizes.sort(Collections.reverseOrder());

    return descImageSizes;
  }

  /**
   * Get the smallest artwork if different sizes are available or null
   * 
   * @return the smallest artwork or null
   */
  public ImageSizeAndUrl getSmallestArtwork() {
    if (!imageSizes.isEmpty()) {
      List<ImageSizeAndUrl> ascImageSizes = new ArrayList<>(imageSizes);

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
   * Get the biggest artwork if different sizes are available or null
   * 
   * @return the biggest artwork or null
   */
  public ImageSizeAndUrl getBiggestArtwork() {
    if (!imageSizes.isEmpty()) {
      List<ImageSizeAndUrl> descImageSizes = new ArrayList<>(imageSizes);

      // sort descending
      descImageSizes.sort(Collections.reverseOrder());
      ImageSizeAndUrl biggestImage = descImageSizes.get(0);
      if (biggestImage != null) {
        return biggestImage;
      }
    }
    return null;
  }

  /**
   * Get the size order to indicate how big this artwork is
   * 
   * @return the size order
   */
  public int getSizeOrder() {
    return sizeOrder;
  }

  /**
   * Set the size order to indicate how big this artwork is
   * 
   * @param sizeOrder
   *          the size order
   */
  public void setSizeOrder(int sizeOrder) {
    this.sizeOrder = sizeOrder;
  }

  /**
   * Get the likes of this artwork to indicate how popular this artwork is
   * 
   * @return the likes
   */
  public int getLikes() {
    return likes;
  }

  /**
   * amount of likes (or other, for ordering)
   * 
   * @param likes
   *          set the amount of likes
   */
  public void setLikes(int likes) {
    this.likes = likes;
  }

  /**
   * is this an animated graphic?
   * 
   * @return true if the graphic is animated; false otherwise
   */
  public boolean isAnimated() {
    return animated;
  }

  /**
   * set this graphic as animated
   * 
   * @param animated
   *          the animated state
   */
  public void setAnimated(boolean animated) {
    this.animated = animated;
  }

  /**
   * Get the season (useful for season artwork)
   * 
   * @return the season
   */
  public int getSeason() {
    return season;
  }

  /**
   * Set the season (useful for season artwork)
   * 
   * @param season
   *          the season
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
   * This class represents a combination of image size and the correspondin url for an artwork
   * 
   * @author Manuel Laggner
   * @since 1.0
   */
  public static class ImageSizeAndUrl implements Comparable<ImageSizeAndUrl> {
    private int    width  = 0;
    private int    height = 0;
    private String url    = "";

    public ImageSizeAndUrl(int width, int height, String url) {
      this.width = width;
      this.height = height;
      this.url = StrgUtils.getNonNullString(url);
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
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ImageSizeAndUrl that = (ImageSizeAndUrl) o;
      return width == that.width && height == that.height && url.equals(that.url);
    }

    @Override
    public int hashCode() {
      return Objects.hash(width, height, url);
    }

    @Override
    public String toString() {
      return width + "x" + height;
    }
  }

  public static class MediaArtworkComparator implements Comparator<MediaArtwork> {
    private String preferredLangu = "en";

    public MediaArtworkComparator(String language) {
      preferredLangu = language;
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(MediaArtwork arg0, MediaArtwork arg1) {
      // check first if is preferred langu
      if (Objects.equals(preferredLangu, arg0.getLanguage()) && !Objects.equals(preferredLangu, arg1.getLanguage())) {
        return -1;
      }
      if (!Objects.equals(preferredLangu, arg0.getLanguage()) && Objects.equals(preferredLangu, arg1.getLanguage())) {
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
