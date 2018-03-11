/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class MovieSetEditorDialog. Edit movie sets
 * 
 * @author Manuel Laggner
 */
public class MovieSetEditorDialog extends TmmDialog {
  private static final long   serialVersionUID = -4446433759280691976L;
  private static final Logger LOGGER           = LoggerFactory.getLogger(MovieSetEditorDialog.class);

  private MovieList           movieList        = MovieList.getInstance();
  private MovieSet            movieSetToEdit;
  private List<Movie>         moviesInSet      = ObservableCollections.observableList(new ArrayList<Movie>());
  private List<Movie>         removedMovies    = new ArrayList<>();
  private List<MediaScraper>  artworkScrapers  = new ArrayList<>();
  private boolean             continueQueue    = true;

  /** UI components */
  private JTextField          tfName;
  private JTable              tableMovies;
  private ImageLabel          lblPoster;
  private ImageLabel          lblFanart;
  private JTextPane           tpOverview;
  private JTextField          tfTmdbId;
  private ImageLabel          lblLogo;
  private ImageLabel          lblClearlogo;
  private ImageLabel          lblBanner;
  private ImageLabel          lblClearart;

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   * @param inQueue
   *          the in queue
   */
  public MovieSetEditorDialog(MovieSet movieSet, boolean inQueue) {
    super(BUNDLE.getString("movieset.edit"), "movieSetEditor"); //$NON-NLS-1$

    movieSetToEdit = movieSet;
    try {
      List<String> enabledScrapers = new ArrayList<>();
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(Constants.TMDB)) {
        enabledScrapers.add(Constants.TMDB);
      }
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(Constants.FANART_TV)) {
        enabledScrapers.add(Constants.FANART_TV);
      }
      artworkScrapers.addAll(movieList.getArtworkScrapers(enabledScrapers));
      // artworkScrapers.addAll(movieList.getArtworkScrapers(Arrays.asList(Constants.TMDB, Constants.FANART_TV)));
    }
    catch (Exception e2) {
      LOGGER.warn("error getting IMediaArtworkProvider " + e2.getMessage());
    }

    {
      JTabbedPane tabbedPane = new MainTabbedPane() {
        private static final long serialVersionUID = 71548865608767532L;

        @Override
        public void updateUI() {
          putClientProperty("bottomBorder", Boolean.FALSE);
          super.updateUI();
        }
      };
      getContentPane().add(tabbedPane, BorderLayout.CENTER);

      JPanel panelContent = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), panelContent); //$NON-NLS-1$
      panelContent.setLayout(new MigLayout("", "[][400lp,grow 200][150lp:200lp,grow 50]", "[][][150lp:200lp,grow][20lp:n][100lp:150lp,grow]"));

      JLabel lblName = new TmmLabel(BUNDLE.getString("movieset.title")); //$NON-NLS-1$
      panelContent.add(lblName, "cell 0 0,alignx right");

      tfName = new JTextField();
      panelContent.add(tfName, "cell 1 0,growx,aligny top");
      tfName.setColumns(10);

      lblPoster = new ImageLabel();
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int tmdbId = 0;
          try {
            tmdbId = Integer.parseInt(tfTmdbId.getText());
          }
          catch (Exception ignored) {
          }
          HashMap<String, Object> ids = new HashMap<>(movieSetToEdit.getIds());
          ids.put(Constants.TMDB, tmdbId);
          // MovieSetImageChooserDialog dialog = new MovieSetImageChooserDialog(tmdbId, ImageType.POSTER, lblPoster);
          ImageChooserDialog dialog = new ImageChooserDialog(ids, ImageType.POSTER, artworkScrapers, lblPoster, null, null, MediaType.MOVIE_SET);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      panelContent.add(lblPoster, "cell 2 0 1 3,grow");

      JLabel lblTmdbid = new TmmLabel(BUNDLE.getString("metatag.tmdb")); //$NON-NLS-1$
      panelContent.add(lblTmdbid, "cell 0 1,alignx right");

      tfTmdbId = new JTextField();
      panelContent.add(tfTmdbId, "flowx,cell 1 1,aligny center");
      tfTmdbId.setColumns(10);

      JLabel lblOverview = new TmmLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      panelContent.add(lblOverview, "cell 0 2,alignx right,aligny top");

      JScrollPane scrollPaneOverview = new JScrollPane();
      panelContent.add(scrollPaneOverview, "cell 1 2,grow");

      tpOverview = new JTextPane();
      tpOverview.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
      scrollPaneOverview.setViewportView(tpOverview);

      JLabel lblMovies = new TmmLabel(BUNDLE.getString("tmm.movies")); //$NON-NLS-1$
      panelContent.add(lblMovies, "flowy,cell 0 4,alignx right,aligny top");

      JScrollPane scrollPaneMovies = new JScrollPane();
      panelContent.add(scrollPaneMovies, "cell 1 4,grow");

      tableMovies = new TmmTable();
      scrollPaneMovies.setViewportView(tableMovies);

      lblFanart = new ImageLabel();
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int tmdbId = 0;
          try {
            tmdbId = Integer.parseInt(tfTmdbId.getText());
          }
          catch (Exception ignored) {
          }
          HashMap<String, Object> ids = new HashMap<>(movieSetToEdit.getIds());
          ids.put(Constants.TMDB, tmdbId);
          ImageChooserDialog dialog = new ImageChooserDialog(ids, ImageType.FANART, artworkScrapers, lblFanart, null, null, MediaType.MOVIE_SET);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      panelContent.add(lblFanart, "cell 2 4,grow");

      JButton btnSearchTmdbId = new JButton("");
      btnSearchTmdbId.setAction(new SearchIdAction());
      panelContent.add(btnSearchTmdbId, "cell 1 1");

      JButton btnRemoveMovie = new JButton("");
      btnRemoveMovie.setAction(new RemoveMovieAction());
      panelContent.add(btnRemoveMovie, "cell 0 4,alignx right,aligny top");

      /**
       * Artwork pane
       */
      {
        JPanel artworkPanel = new JPanel();
        tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null);
        artworkPanel.setLayout(new MigLayout("", "[200lp:300lp,grow][200lp:300lp,grow]",
            "[][100lp:125lp,grow][20lp:n][][100lp:125lp,grow][20lp:n][][100lp:150lp,grow]"));
        {
          JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
          artworkPanel.add(lblLogoT, "cell 0 0");
        }
        {
          lblLogo = new ImageLabel();
          lblLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(movieSetToEdit.getIds(), ImageType.LOGO, movieList.getDefaultArtworkScrapers(),
                  lblLogo, null, null, MediaType.MOVIE);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
          });
          {
            final JLabel lblClearlogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo")); //$NON-NLS-1$
            artworkPanel.add(lblClearlogoT, "cell 1 0");
          }
          lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblLogo, "cell 0 1,grow");
        }
        {
          lblClearlogo = new ImageLabel();
          lblClearlogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(movieSetToEdit.getIds(), ImageType.CLEARLOGO, movieList.getDefaultArtworkScrapers(),
                  lblClearlogo, null, null, MediaType.MOVIE);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
          });
          lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblClearlogo, "cell 1 1,grow");
        }
        {
          JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
          artworkPanel.add(lblBannerT, "cell 0 3,growx,aligny top");
        }
        {
          lblBanner = new ImageLabel();
          lblBanner.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(movieSetToEdit.getIds(), ImageType.BANNER, movieList.getDefaultArtworkScrapers(),
                  lblBanner, null, null, MediaType.MOVIE);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
          });
          lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblBanner, "cell 0 4 2 1,grow");
        }

        // extra artwork
        lblBanner.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.BANNER));

        {
          JLabel lblClearartT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart")); //$NON-NLS-1$
          artworkPanel.add(lblClearartT, "cell 0 6,growx,aligny top");
        }
        {
          lblClearart = new ImageLabel();
          lblClearart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(movieSetToEdit.getIds(), ImageType.CLEARART, movieList.getDefaultArtworkScrapers(),
                  lblClearart, null, null, MediaType.MOVIE);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
          });
          lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblClearart, "cell 0 7,grow");
        }
      }
    }
    /**
     * Button pane
     */
    {
      if (inQueue) {
        JButton abortButton = new JButton(new AbortAction());
        addButton(abortButton);
      }

      JButton btnCancel = new JButton(new CancelAction());
      addButton(btnCancel);

      JButton btnOk = new JButton(new OkAction());
      addDefaultButton(btnOk);
    }

    {
      tfName.setText(movieSetToEdit.getTitle());
      tfTmdbId.setText(String.valueOf(movieSetToEdit.getTmdbId()));
      tpOverview.setText(movieSetToEdit.getPlot());
      moviesInSet.addAll(movieSetToEdit.getMovies());

      if (StringUtils.isNotBlank(movieSetToEdit.getArtworkFilename(MediaFileType.POSTER))) {
        lblPoster.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.POSTER));
      }
      else {
        lblPoster.setImageUrl(movieSetToEdit.getArtworkUrl(MediaFileType.POSTER));
      }

      if (StringUtils.isNotBlank(movieSetToEdit.getArtworkFilename(MediaFileType.FANART))) {
        lblFanart.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.FANART));
      }
      else {
        lblFanart.setImageUrl(movieSetToEdit.getArtworkUrl(MediaFileType.FANART));
      }
      lblLogo.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.LOGO));
      lblClearlogo.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.CLEARLOGO));
      lblClearart.setImagePath(movieSetToEdit.getArtworkFilename(MediaFileType.CLEARART));
    }

    initDataBindings();

    // adjust table columns
    // name column
    tableMovies.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name"));

    // year column
    int width = tableMovies.getFontMetrics(tableMovies.getFont()).stringWidth(" 2000");
    int titleWidth = tableMovies.getFontMetrics(tableMovies.getFont()).stringWidth(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    if (titleWidth > width) {
      width = titleWidth;
    }
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(width);
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setMinWidth(width);
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setMaxWidth((int) (width * 1.5));
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.year"));

    // watched column
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(70);
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setMinWidth(70);
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(85);
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.watched"));
  }

  private class RemoveMovieAction extends AbstractAction {
    private static final long serialVersionUID = 8013039811395731218L;

    RemoveMovieAction() {
      putValue(LARGE_ICON_KEY, IconManager.REMOVE_INV);
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.movie.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (moviesInSet.isEmpty()) {
        return;
      }

      int row = tableMovies.getSelectedRow();
      if (row > -1) {
        Movie movie = moviesInSet.get(row);
        moviesInSet.remove(row);
        removedMovies.add(movie);
      }
    }
  }

  private class OkAction extends AbstractAction {
    private static final long serialVersionUID = -7322270015667230646L;

    OkAction() {
      putValue(NAME, BUNDLE.getString("Button.save")); //$NON-NLS-1$ );
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.save")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      movieSetToEdit.setTitle(tfName.getText());
      movieSetToEdit.setPlot(tpOverview.getText());

      // image changes
      if (StringUtils.isNotEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.POSTER))) {
        movieSetToEdit.setArtworkUrl(lblPoster.getImageUrl(), MediaFileType.POSTER);
      }
      if (StringUtils.isNotEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.FANART))) {
        movieSetToEdit.setArtworkUrl(lblFanart.getImageUrl(), MediaFileType.FANART);
      }

      if (!StringUtils.isEmpty(lblLogo.getImageUrl()) && !lblLogo.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.LOGO))) {
        movieSetToEdit.setArtworkUrl(lblLogo.getImageUrl(), MediaFileType.LOGO);
      }

      if (!StringUtils.isEmpty(lblClearlogo.getImageUrl())
          && !lblClearlogo.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.CLEARLOGO))) {
        movieSetToEdit.setArtworkUrl(lblClearlogo.getImageUrl(), MediaFileType.CLEARLOGO);
      }

      if (!StringUtils.isEmpty(lblBanner.getImageUrl()) && !lblBanner.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.BANNER))) {
        movieSetToEdit.setArtworkUrl(lblBanner.getImageUrl(), MediaFileType.BANNER);
      }

      if (!StringUtils.isEmpty(lblClearart.getImageUrl())
          && !lblClearart.getImageUrl().equals(movieSetToEdit.getArtworkUrl(MediaFileType.CLEARART))) {
        movieSetToEdit.setArtworkUrl(lblClearart.getImageUrl(), MediaFileType.CLEARART);
      }

      // delete movies
      for (int i = movieSetToEdit.getMovies().size() - 1; i >= 0; i--) {
        Movie movie = movieSetToEdit.getMovies().get(i);
        if (!moviesInSet.contains(movie)) {
          movie.setMovieSet(null);
          movie.writeNFO();
          movie.saveToDb();
          movieSetToEdit.removeMovie(movie, true);
        }
      }

      // sort movies in the right order
      for (int i = 0; i < moviesInSet.size(); i++) {
        Movie movie = moviesInSet.get(i);
        movie.saveToDb();
        movie.writeNFO();
      }

      // remove removed movies
      for (Movie movie : removedMovies) {
        movie.removeFromMovieSet();
        movie.saveToDb();
        movie.writeNFO();
        movieSetToEdit.removeMovie(movie, true);
      }

      MovieList.getInstance().sortMoviesInMovieSet(movieSetToEdit);

      // and rewrite NFO
      for (Movie movie : moviesInSet) {
        movie.writeNFO();
      }

      int tmdbId = 0;
      try {

        tmdbId = Integer.parseInt(tfTmdbId.getText());
      }
      catch (Exception ignored) {
      }
      movieSetToEdit.setTmdbId(tmdbId);
      movieSetToEdit.saveToDb();

      setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -6214112833170817002L;

    CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class AbortAction extends AbstractAction {
    private static final long serialVersionUID = 1215596133205394653L;

    AbortAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.STOP_INV);
      putValue(LARGE_ICON_KEY, IconManager.STOP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }

  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<Movie, List<Movie>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, moviesInSet, tableMovies);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("title");
    jTableBinding.addColumnBinding(movieBeanProperty).setEditable(false); // $NON-NLS-1$
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setEditable(false); // $NON-NLS-1$
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("watched");
    jTableBinding.addColumnBinding(movieBeanProperty_2).setEditable(false).setColumnClass(Boolean.class); // $NON-NLS-1$
    //
    jTableBinding.setEditable(false);
    bindings.add(jTableBinding);
    jTableBinding.bind();
  }

  private class SearchIdAction extends AbstractAction {
    private static final long serialVersionUID = -8980803676368394987L;

    SearchIdAction() {
      putValue(NAME, BUNDLE.getString("movieset.tmdb.find")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.tmdb.desc")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // search for a tmdbId
      try {
        List<MediaScraper> sets = MediaScraper.getMediaScrapers(ScraperType.MOVIE_SET);
        if (sets != null && sets.size() > 0) {
          MediaScraper first = sets.get(0); // just get first
          IMovieSetMetadataProvider mp = (IMovieSetMetadataProvider) first.getMediaProvider();

          for (Movie movie : moviesInSet) {
            MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
            if (Utils.isValidImdbId(movie.getImdbId()) || movie.getTmdbId() > 0) {
              options.setTmdbId(movie.getTmdbId());
              options.setImdbId(movie.getImdbId());
              options.setLanguage(LocaleUtils.toLocale(MovieModuleManager.SETTINGS.getScraperLanguage().name()));
              options.setCountry(MovieModuleManager.SETTINGS.getCertificationCountry());
              MediaMetadata md = mp.getMetadata(options);
              if ((int) md.getId(MediaMetadata.TMDB_SET) > 0) {
                tfTmdbId.setText(String.valueOf(md.getId(MediaMetadata.TMDB_SET)));
                break;
              }
            }
          }
        }
      }
      catch (Exception e1) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("movieset.tmdb.error")); //$NON-NLS-1$
      }

    }
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }
}
