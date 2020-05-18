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
package org.tinymediamanager.ui.movies;

import java.awt.FontMetrics;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;

import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.TmmDateFormat;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieComparator;
import org.tinymediamanager.core.movie.MovieEdition;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.table.TmmTableFormat;
import org.tinymediamanager.ui.renderer.DateTableCellRenderer;
import org.tinymediamanager.ui.renderer.RightAlignTableCellRenderer;

/**
 * The MovieTableFormat. Used as definition for the movie table in the movie module
 *
 * @author Manuel Laggner
 */
public class MovieTableFormat extends TmmTableFormat<Movie> {
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  public MovieTableFormat() {

    Comparator<Movie> movieComparator = new MovieComparator();
    Comparator<Movie> originalTitleComparator = new MovieComparator() {
      @Override
      public int compare(Movie movie1, Movie movie2) {
        if (stringCollator != null) {
          String titleMovie1 = StrgUtils.normalizeString(movie1.getOriginalTitleSortable().toLowerCase(Locale.ROOT));
          String titleMovie2 = StrgUtils.normalizeString(movie2.getOriginalTitleSortable().toLowerCase(Locale.ROOT));
          return stringCollator.compare(titleMovie1, titleMovie2);
        }
        return movie1.getOriginalTitleSortable().toLowerCase(Locale.ROOT).compareTo(movie2.getOriginalTitleSortable().toLowerCase(Locale.ROOT));
      }
    };
    Comparator<String> stringComparator = new StringComparator();
    Comparator<Float> floatComparator = new FloatComparator();
    Comparator<ImageIcon> imageComparator = new ImageComparator();
    Comparator<Date> dateComparator = new DateComparator();
    Comparator<String> videoFormatComparator = new VideoFormatComparator();
    Comparator<String> fileSizeComparator = new FileSizeComparator();
    Comparator<Integer> integerComparator = new IntegerComparator();
    Comparator<MediaCertification> certificationComparator = new CertificationComparator();

    FontMetrics fontMetrics = getFontMetrics();

    /*
     * title
     */
    Column col = new Column(BUNDLE.getString("metatag.title"), "title", movie -> movie, Movie.class);
    col.setColumnComparator(movieComparator);
    col.setCellRenderer(new MovieBorderTableCellRenderer());
    col.setColumnTooltip(Movie::getTitleSortable);
    addColumn(col);

    /*
     * original title (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.originaltitle"), "originalTitle", movie -> movie, Movie.class);
    col.setColumnComparator(originalTitleComparator);
    col.setCellRenderer(new MovieBorderTableCellRenderer());
    col.setColumnTooltip(Movie::getOriginalTitleSortable);
    addColumn(col);

    /*
     * sorttitle (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.sorttitle"), "sortTitle", Movie::getSortTitle, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    col.setColumnTooltip(Movie::getSortTitle);
    addColumn(col);

    /*
     * year
     */
    col = new Column(BUNDLE.getString("metatag.year"), "year", MediaEntity::getYear, Movie.class);
    col.setColumnComparator(integerComparator);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("2000") * 1.3f));
    addColumn(col);

    /*
     * file name (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.filename"), "filename", movie -> movie.getMainVideoFile().getFilename(), String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    col.setColumnTooltip(movie -> movie.getMainVideoFile().getFilename());
    addColumn(col);

    /*
     * folder name (hidden per default)
     */
    Function<Movie, String> pathFunction = movie -> movie.getPathNIO().toString();
    col = new Column(BUNDLE.getString("metatag.path"), "path", pathFunction, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    col.setColumnTooltip(pathFunction);
    addColumn(col);

    /*
     * movie set (hidden per default)
     */
    Function<Movie, String> movieSetFunction = movie -> movie.getMovieSet() == null ? null : movie.getMovieSet().getTitle();
    col = new Column(BUNDLE.getString("metatag.movieset"), "movieset", movieSetFunction, String.class);
    col.setColumnComparator(stringComparator);
    col.setColumnResizeable(true);
    col.setColumnTooltip(movieSetFunction);
    addColumn(col);

    /*
     * rating
     */
    col = new Column(BUNDLE.getString("metatag.rating"), "rating", movie -> movie.getRating().getRating(), Float.class);
    col.setColumnComparator(floatComparator);
    col.setHeaderIcon(IconManager.RATING);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("99.9") * 1.2f));
    col.setColumnTooltip(
        movie -> movie.getRating().getRating() + " (" + movie.getRating().getVotes() + " " + BUNDLE.getString("metatag.votes") + ")");
    addColumn(col);

    /*
     * votes (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.votes"), "votes", movie -> movie.getRating().getVotes(), Integer.class);
    col.setColumnComparator(integerComparator);
    col.setHeaderIcon(IconManager.VOTES);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("1000000") * 1.2f));
    addColumn(col);

    /*
     * certification (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.certification"), "certification", Movie::getCertification, MediaCertification.class);
    col.setColumnComparator(certificationComparator);
    col.setHeaderIcon(IconManager.CERTIFICATION);
    col.setColumnResizeable(true);
    addColumn(col);

    /*
     * date added
     */
    col = new Column(BUNDLE.getString("metatag.dateadded"), "dateAdded", MediaEntity::getDateAddedForUi, Date.class);
    col.setColumnComparator(dateComparator);
    col.setHeaderIcon(IconManager.DATE_ADDED);
    col.setCellRenderer(new DateTableCellRenderer());
    col.setColumnResizeable(false);
    try {
      Date date = StrgUtils.parseDate("2012-12-12");
      col.setMinWidth((int) (fontMetrics.stringWidth(TmmDateFormat.MEDIUM_DATE_FORMAT.format(date)) * 1.2f));
    }
    catch (Exception ignored) {
    }
    addColumn(col);

    /*
     * video format (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.format"), "videoFormat", Movie::getMediaInfoVideoFormat, String.class);
    col.setColumnComparator(videoFormatComparator);
    col.setHeaderIcon(IconManager.VIDEO_FORMAT);
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("1080p") * 1.2f));
    addColumn(col);

    /*
     * audio codec and channels(hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.audio"), "audio", movie -> {
      List<MediaFile> videos = movie.getMediaFiles(MediaFileType.VIDEO);
      if (!videos.isEmpty()) {
        MediaFile mediaFile = videos.get(0);
        if (StringUtils.isNotBlank(mediaFile.getAudioCodec())) {
          return mediaFile.getAudioCodec() + " " + mediaFile.getAudioChannels();
        }
      }
      return "";
    }, String.class);
    col.setColumnComparator(stringComparator);
    col.setHeaderIcon(IconManager.AUDIO);
    col.setMinWidth((int) (fontMetrics.stringWidth("DTS 7ch") * 1.2f));
    addColumn(col);

    /*
     * main video file size (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.size"), "fileSize", movie -> {
      long size = 0;
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
        size += mf.getFilesize();
      }
      return (int) (size / (1000.0 * 1000.0)) + " M";
    }, String.class);
    col.setColumnComparator(fileSizeComparator);
    col.setHeaderIcon(IconManager.FILE_SIZE);
    col.setCellRenderer(new RightAlignTableCellRenderer());
    col.setColumnResizeable(false);
    col.setMinWidth((int) (fontMetrics.stringWidth("50000M") * 1.2f));
    addColumn(col);

    /*
     * Edition (hidden per default)
     */
    Function<Movie, String> movieEditionFunction = movie -> movie.getEdition() == null || movie.getEdition() == MovieEdition.NONE ? null
        : movie.getEdition().toString();
    col = new Column(BUNDLE.getString("metatag.edition"), "edition", movieEditionFunction, String.class);
    col.setColumnComparator(stringComparator);
    col.setHeaderIcon(IconManager.EDITION);
    col.setColumnTooltip(movieEditionFunction);
    addColumn(col);

    /*
     * Source (hidden per default)
     */
    Function<Movie, String> mediaSourceFunction = movie -> movie.getMediaSource() == null ? null : movie.getMediaSource().toString();
    col = new Column(BUNDLE.getString("metatag.source"), "mediaSource", mediaSourceFunction, String.class);
    col.setColumnComparator(stringComparator);
    col.setHeaderIcon(IconManager.SOURCE);
    col.setColumnTooltip(mediaSourceFunction);
    addColumn(col);

    /*
     * 3D (hidden per default)
     */
    col = new Column(BUNDLE.getString("metatag.3d"), "video3d", movie -> getCheckIcon(movie.isVideoIn3D()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.VIDEO_3D);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * NFO
     */
    col = new Column(BUNDLE.getString("tmm.nfo"), "nfo", movie -> getCheckIcon(movie.getHasNfoFile()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.NFO);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * images
     */
    col = new Column(BUNDLE.getString("tmm.images"), "images", movie -> getCheckIcon(movie.getHasImages()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.IMAGES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * trailer
     */
    col = new Column(BUNDLE.getString("tmm.trailer"), "trailer", movie -> getCheckIcon(movie.getHasTrailer()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.TRAILER);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * subtitles
     */
    col = new Column(BUNDLE.getString("tmm.subtitles"), "subtitles", movie -> getCheckIcon(movie.getHasSubtitles()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.SUBTITLES);
    col.setColumnResizeable(false);
    addColumn(col);

    /*
     * watched
     */
    col = new Column(BUNDLE.getString("metatag.watched"), "watched", movie -> getCheckIcon(movie.isWatched()), ImageIcon.class);
    col.setColumnComparator(imageComparator);
    col.setHeaderIcon(IconManager.WATCHED);
    col.setColumnResizeable(false);
    addColumn(col);
  }
}
