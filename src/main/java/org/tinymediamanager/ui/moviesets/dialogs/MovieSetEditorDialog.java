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
package org.tinymediamanager.ui.moviesets.dialogs;

import static org.tinymediamanager.ui.TmmUIHelper.createLinkForImage;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.components.FlatButton;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.LinkLabel;
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
  private static final long   serialVersionUID    = -4446433759280691976L;
  private static final Logger LOGGER              = LoggerFactory.getLogger(MovieSetEditorDialog.class);
  private static final String ORIGINAL_IMAGE_SIZE = "originalImageSize";
  private static final String SPACER              = "        ";

  private MovieList           movieList           = MovieList.getInstance();
  private MovieSet            movieSetToEdit;
  private List<Movie>         moviesInSet         = ObservableCollections.observableList(new ArrayList<>());
  private List<Movie>         removedMovies       = new ArrayList<>();
  private List<MediaScraper>  artworkScrapers     = new ArrayList<>();
  private boolean             continueQueue       = true;
  private boolean             navigateBack        = false;
  private int                 queueIndex;
  private int                 queueSize;

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
  private ImageLabel          lblDisc;
  private ImageLabel          lblThumb;

  private JTextField          tfPoster;
  private JTextField          tfFanart;
  private JTextField          tfLogo;
  private JTextField          tfClearLogo;
  private JTextField          tfBanner;
  private JTextField          tfClearArt;
  private JTextField          tfThumb;
  private JTextField          tfDisc;

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public MovieSetEditorDialog(MovieSet movieSet, int queueIndex, int queueSize) {
    super(BUNDLE.getString("movieset.edit") + (queueSize > 1 ? " " + (queueIndex + 1) + "/" + queueSize : ""), "movieSetEditor");

    movieSetToEdit = movieSet;
    this.queueIndex = queueIndex;
    this.queueSize = queueSize;

    try {
      List<String> enabledScrapers = new ArrayList<>();
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(Constants.TMDB)) {
        enabledScrapers.add(Constants.TMDB);
      }
      if (MovieModuleManager.SETTINGS.getArtworkScrapers().contains(Constants.FANART_TV)) {
        enabledScrapers.add(Constants.FANART_TV);
      }
      artworkScrapers.addAll(movieList.getArtworkScrapers(enabledScrapers));
    }
    catch (Exception e2) {
      LOGGER.warn("error getting IMediaArtworkProvider - {}", e2.getMessage());
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
      tabbedPane.addTab(BUNDLE.getString("metatag.details"), panelContent);
      panelContent.setLayout(new MigLayout("", "[][400lp,grow 200][150lp:200lp,grow 50]", "[][][150lp:200lp,grow][20lp:n][][][50lp:100lp,grow]"));

      JLabel lblName = new TmmLabel(BUNDLE.getString("movieset.title"));
      panelContent.add(lblName, "cell 0 0,alignx right");

      tfName = new JTextField();
      panelContent.add(tfName, "cell 1 0,growx,aligny top");
      tfName.setColumns(10);

      lblPoster = new ImageLabel();
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.POSTER, artworkScrapers, lblPoster,
              null, null, MediaType.MOVIE_SET);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
          updateArtworkUrl(lblPoster, tfPoster);
        }
      });
      panelContent.add(new TmmLabel(BUNDLE.getString("mediafiletype.poster")), "cell 2 0");

      LinkLabel lblPosterSize = new LinkLabel();
      panelContent.add(lblPosterSize, "cell 2 0");

      JButton btnDeletePoster = new FlatButton(SPACER, IconManager.DELETE_GRAY);
      btnDeletePoster.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
      btnDeletePoster.addActionListener(e -> {
        lblPoster.clearImage();
        tfPoster.setText("");
      });
      panelContent.add(btnDeletePoster, "cell 2 0");

      panelContent.add(lblPoster, "cell 2 1 1 4,grow");
      lblPoster.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblPosterSize, lblPoster, MediaFileType.POSTER));

      JLabel lblTmdbid = new TmmLabel(BUNDLE.getString("metatag.tmdb"));
      panelContent.add(lblTmdbid, "cell 0 1,alignx right");

      tfTmdbId = new JTextField();
      panelContent.add(tfTmdbId, "flowx,cell 1 1,aligny center");
      tfTmdbId.setColumns(10);

      JLabel lblOverview = new TmmLabel(BUNDLE.getString("metatag.plot"));
      panelContent.add(lblOverview, "cell 0 2,alignx right,aligny top");

      JScrollPane scrollPaneOverview = new JScrollPane();
      panelContent.add(scrollPaneOverview, "cell 1 2,grow");

      tpOverview = new JTextPane();
      tpOverview.setForeground(UIManager.getColor("TextField.foreground"));
      scrollPaneOverview.setViewportView(tpOverview);

      JLabel lblMovies = new TmmLabel(BUNDLE.getString("tmm.movies"));
      panelContent.add(lblMovies, "flowy,cell 0 4,alignx right,aligny top");

      JScrollPane scrollPaneMovies = new JScrollPane();
      panelContent.add(scrollPaneMovies, "cell 1 4 1 3,grow");

      tableMovies = new TmmTable();
      scrollPaneMovies.setViewportView(tableMovies);

      lblFanart = new ImageLabel();
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.FANART, artworkScrapers, lblFanart,
              null, null, MediaType.MOVIE_SET);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
          updateArtworkUrl(lblFanart, tfFanart);
        }
      });
      panelContent.add(new TmmLabel(BUNDLE.getString("mediafiletype.fanart")), "cell 2 5");

      LinkLabel lblFanartSize = new LinkLabel();
      panelContent.add(lblFanartSize, "cell 2 5");
      JButton btnDeleteFanart = new FlatButton(SPACER, IconManager.DELETE_GRAY);
      btnDeleteFanart.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
      btnDeleteFanart.addActionListener(e -> {
        lblFanart.clearImage();
        tfFanart.setText("");
      });
      panelContent.add(btnDeleteFanart, "cell 2 5");

      panelContent.add(lblFanart, "cell 2 6,grow");
      lblFanart.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblFanartSize, lblFanart, MediaFileType.FANART));

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
        artworkPanel.setLayout(new MigLayout("", "[200lp:300lp,grow][20lp:n][200lp:300lp,grow][20lp:n][100lp:200lp,grow]",
            "[][100lp:125lp,grow][20lp:n][][100lp:125lp,grow][20lp:n][][100lp:150lp,grow]"));
        {
          JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo"));
          artworkPanel.add(lblLogoT, "cell 0 0");

          LinkLabel lblLogoSize = new LinkLabel();
          artworkPanel.add(lblLogoSize, "cell 0 0");

          JButton btnDeleteLogo = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteLogo.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteLogo.addActionListener(e -> {
            lblLogo.clearImage();
            tfLogo.setText("");
          });
          artworkPanel.add(btnDeleteLogo, "cell 0 0");

          lblLogo = new ImageLabel();
          lblLogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.LOGO,
                  movieList.getDefaultArtworkScrapers(), lblLogo, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
              updateArtworkUrl(lblLogo, tfLogo);
            }
          });

          lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblLogo, "cell 0 1,grow");
          lblLogo.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblLogoSize, lblLogo, MediaFileType.LOGO));
        }
        {
          final JLabel lblClearlogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo"));
          artworkPanel.add(lblClearlogoT, "cell 2 0");

          LinkLabel lblClearlogoSize = new LinkLabel();
          artworkPanel.add(lblClearlogoSize, "cell 2 0");

          JButton btnDeleteClearLogo = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteClearLogo.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteClearLogo.addActionListener(e -> {
            lblClearlogo.clearImage();
            tfClearLogo.setText("");
          });
          artworkPanel.add(btnDeleteClearLogo, "cell 2 0");

          lblClearlogo = new ImageLabel();
          lblClearlogo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.CLEARLOGO,
                  movieList.getDefaultArtworkScrapers(), lblClearlogo, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
            }
          });
          lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblClearlogo, "cell 2 1,grow");
          lblClearlogo.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE,
              e -> setImageSizeAndCreateLink(lblClearlogoSize, lblClearlogo, MediaFileType.CLEARLOGO));
        }
        {
          JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner"));
          artworkPanel.add(lblBannerT, "cell 0 3");

          LinkLabel lblBannerSize = new LinkLabel();
          artworkPanel.add(lblBannerSize, "cell 0 3");

          JButton btnDeleteBanner = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteBanner.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteBanner.addActionListener(e -> {
            lblBanner.clearImage();
            tfBanner.setText("");
          });
          artworkPanel.add(btnDeleteBanner, "cell 0 3");

          lblBanner = new ImageLabel();
          lblBanner.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.BANNER,
                  movieList.getDefaultArtworkScrapers(), lblBanner, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
              updateArtworkUrl(lblBanner, tfBanner);
            }
          });
          lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblBanner, "cell 0 4 3 1,grow");
          lblBanner.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblBannerSize, lblBanner, MediaFileType.BANNER));
        }
        {
          JLabel lblClearartT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart"));
          artworkPanel.add(lblClearartT, "cell 0 6");

          LinkLabel lblClearartSize = new LinkLabel();
          artworkPanel.add(lblClearartSize, "cell 0 6");

          JButton btnDeleteClearart = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteClearart.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteClearart.addActionListener(e -> {
            lblClearart.clearImage();
            tfClearArt.setText("");
          });
          artworkPanel.add(btnDeleteClearart, "cell 0 6");

          lblClearart = new ImageLabel();
          lblClearart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.CLEARART,
                  movieList.getDefaultArtworkScrapers(), lblClearart, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
              updateArtworkUrl(lblClearart, tfClearArt);
            }
          });
          lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblClearart, "cell 0 7,grow");
          lblClearart.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE,
              e -> setImageSizeAndCreateLink(lblClearartSize, lblClearart, MediaFileType.CLEARART));
        }
        {
          JLabel lblThumbT = new TmmLabel("Thumb");
          artworkPanel.add(lblThumbT, "cell 2 6");

          LinkLabel lblThumbSize = new LinkLabel();
          artworkPanel.add(lblThumbSize, "cell 2 6");

          JButton btnDeleteThumb = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteThumb.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteThumb.addActionListener(e -> {
            lblThumb.clearImage();
            tfThumb.setText("");
          });
          artworkPanel.add(btnDeleteThumb, "cell 2 6");

          lblThumb = new ImageLabel();
          lblThumb.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.THUMB,
                  movieList.getDefaultArtworkScrapers(), lblThumb, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
              updateArtworkUrl(lblThumb, tfThumb);
            }
          });
          lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblThumb, "cell 2 7,grow");
          lblThumb.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblThumbSize, lblThumb, MediaFileType.THUMB));
        }
        {
          JLabel lblDiscT = new TmmLabel("Disc");
          artworkPanel.add(lblDiscT, "cell 4 6");

          LinkLabel lblDiscSize = new LinkLabel();
          artworkPanel.add(lblDiscSize, "cell 4 6");

          JButton btnDeleteDisc = new FlatButton(SPACER, IconManager.DELETE_GRAY);
          btnDeleteDisc.setToolTipText(BUNDLE.getString("Button.deleteartwork.desc"));
          btnDeleteDisc.addActionListener(e -> {
            lblDisc.clearImage();
            tfDisc.setText("");
          });
          artworkPanel.add(btnDeleteDisc, "cell 4 6");

          lblDisc = new ImageLabel();
          lblDisc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              ImageChooserDialog dialog = new ImageChooserDialog(MovieSetEditorDialog.this, extractIds(), ImageType.DISC,
                  movieList.getDefaultArtworkScrapers(), lblDisc, null, null, MediaType.MOVIE_SET);
              dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
              dialog.setVisible(true);
              updateArtworkUrl(lblDisc, tfDisc);
            }
          });
          lblDisc.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          artworkPanel.add(lblDisc, "cell 4 7,grow");
          lblDisc.addPropertyChangeListener(ORIGINAL_IMAGE_SIZE, e -> setImageSizeAndCreateLink(lblDiscSize, lblDisc, MediaFileType.DISC));
        }
      }

      /**
       * Artwork url pane
       */
      {
        JPanel artworkAndTrailerPanel = new JPanel();
        tabbedPane.addTab(BUNDLE.getString("edit.artwork"), null, artworkAndTrailerPanel, null);
        artworkAndTrailerPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][][][][]"));
        {
          JLabel lblPosterT = new TmmLabel(BUNDLE.getString("mediafiletype.poster"));
          artworkAndTrailerPanel.add(lblPosterT, "cell 0 0,alignx right");
        }
        {
          tfPoster = new JTextField();
          artworkAndTrailerPanel.add(tfPoster, "cell 1 0,growx");

          JLabel lblFanartT = new TmmLabel(BUNDLE.getString("mediafiletype.fanart"));
          artworkAndTrailerPanel.add(lblFanartT, "cell 0 1,alignx right");

          tfFanart = new JTextField();
          artworkAndTrailerPanel.add(tfFanart, "cell 1 1,growx");
        }
        {
          JLabel lblLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.logo"));
          artworkAndTrailerPanel.add(lblLogoT, "cell 0 2,alignx right");

          tfLogo = new JTextField();
          artworkAndTrailerPanel.add(tfLogo, "cell 1 2,growx");
        }
        {
          JLabel lblClearLogoT = new TmmLabel(BUNDLE.getString("mediafiletype.clearlogo"));
          artworkAndTrailerPanel.add(lblClearLogoT, "cell 0 3,alignx right");

          tfClearLogo = new JTextField();
          artworkAndTrailerPanel.add(tfClearLogo, "cell 1 3,growx");
        }
        {
          JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner"));
          artworkAndTrailerPanel.add(lblBannerT, "cell 0 4,alignx right");

          tfBanner = new JTextField();
          artworkAndTrailerPanel.add(tfBanner, "cell 1 4,growx");
        }
        {
          JLabel lblClearArtT = new TmmLabel(BUNDLE.getString("mediafiletype.clearart"));
          artworkAndTrailerPanel.add(lblClearArtT, "cell 0 5,alignx right");

          tfClearArt = new JTextField();
          artworkAndTrailerPanel.add(tfClearArt, "cell 1 5,growx");
        }
        {
          JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb"));
          artworkAndTrailerPanel.add(lblThumbT, "cell 0 6,alignx right");

          tfThumb = new JTextField();
          artworkAndTrailerPanel.add(tfThumb, "cell 1 6,growx");
        }
        {
          JLabel lblDiscT = new TmmLabel(BUNDLE.getString("mediafiletype.disc"));
          artworkAndTrailerPanel.add(lblDiscT, "cell 0 7,alignx trailing");

          tfDisc = new JTextField();
          artworkAndTrailerPanel.add(tfDisc, "cell 1 7,growx");
        }
      }
    }
    /**
     * Button pane
     */
    {
      if (queueSize > 1) {
        JButton btnAbort = new JButton(new AbortAction());
        addButton(btnAbort);
        if (queueIndex > 0) {
          JButton backButton = new JButton(new NavigateBackAction());
          addButton(backButton);
        }
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

      setArtworkPath(MediaFileType.POSTER, lblPoster);
      setArtworkPath(MediaFileType.FANART, lblFanart);
      setArtworkPath(MediaFileType.BANNER, lblBanner);
      setArtworkPath(MediaFileType.LOGO, lblLogo);
      setArtworkPath(MediaFileType.CLEARLOGO, lblClearlogo);
      setArtworkPath(MediaFileType.CLEARART, lblClearart);
      setArtworkPath(MediaFileType.THUMB, lblThumb);
      setArtworkPath(MediaFileType.DISC, lblDisc);

      tfPoster.setText(movieSetToEdit.getArtworkUrl(MediaFileType.POSTER));
      tfFanart.setText(movieSetToEdit.getArtworkUrl(MediaFileType.FANART));
      tfLogo.setText(movieSetToEdit.getArtworkUrl(MediaFileType.LOGO));
      tfClearLogo.setText(movieSetToEdit.getArtworkUrl(MediaFileType.CLEARLOGO));
      tfClearArt.setText(movieSetToEdit.getArtworkUrl(MediaFileType.CLEARART));
      tfThumb.setText(movieSetToEdit.getArtworkUrl(MediaFileType.THUMB));
      tfDisc.setText(movieSetToEdit.getArtworkUrl(MediaFileType.DISC));
      tfBanner.setText(movieSetToEdit.getArtworkUrl(MediaFileType.BANNER));
    }

    bindingGroup = initDataBindings();

    // adjust table columns
    // name column
    tableMovies.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name"));

    // year column
    int width = tableMovies.getFontMetrics(tableMovies.getFont()).stringWidth(" 2000");
    int titleWidth = tableMovies.getFontMetrics(tableMovies.getFont()).stringWidth(BUNDLE.getString("metatag.year"));
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

  private Map<String, Object> extractIds() {
    HashMap<String, Object> ids = new HashMap<>(movieSetToEdit.getIds());

    try {
      // try to parse the TMDB set id
      ids.put(MediaMetadata.TMDB_SET, Integer.parseInt(tfTmdbId.getText()));
    }
    catch (Exception ignored) {
      // nothing to do here
    }

    return ids;
  }

  private void updateArtworkUrl(ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
      textField.setText(imageLabel.getImageUrl());
    }
  }

  private void setArtworkPath(MediaFileType type, ImageLabel imageLabel) {
    if (StringUtils.isNotBlank(movieSetToEdit.getArtworkFilename(type))) {
      imageLabel.setImagePath(movieSetToEdit.getArtworkFilename(type));
    }
    else {
      imageLabel.setImageUrl(movieSetToEdit.getArtworkUrl(type));
    }
  }

  private class RemoveMovieAction extends AbstractAction {
    private static final long serialVersionUID = 8013039811395731218L;

    RemoveMovieAction() {
      putValue(LARGE_ICON_KEY, IconManager.REMOVE_INV);
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.movie.remove"));
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
      putValue(NAME, BUNDLE.getString("Button.save"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.save"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      movieSetToEdit.setTitle(tfName.getText());
      movieSetToEdit.setPlot(tpOverview.getText());

      // process artwork
      processArtwork(MediaFileType.POSTER, lblPoster, tfPoster);
      processArtwork(MediaFileType.FANART, lblFanart, tfFanart);
      processArtwork(MediaFileType.LOGO, lblLogo, tfLogo);
      processArtwork(MediaFileType.CLEARLOGO, lblClearlogo, tfClearLogo);
      processArtwork(MediaFileType.BANNER, lblBanner, tfBanner);
      processArtwork(MediaFileType.CLEARART, lblClearart, tfClearArt);
      processArtwork(MediaFileType.THUMB, lblThumb, tfThumb);
      processArtwork(MediaFileType.DISC, lblDisc, tfDisc);

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
      for (Movie movie : moviesInSet) {
        movie.saveToDb();
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

  private void processArtwork(MediaFileType type, ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isAllBlank(imageLabel.getImagePath(), imageLabel.getImageUrl())
        && StringUtils.isNotBlank(movieSetToEdit.getArtworkFilename(type))) {
      // artwork has been explicitly deleted - we need to remove the artwork url too, since this is a fallback
      movieSetToEdit.removeArtworkUrl(type);
      movieSetToEdit.deleteMediaFiles(type);
    }

    if (StringUtils.isNotEmpty(textField.getText()) && !textField.getText().equals(movieSetToEdit.getArtworkUrl(type))) {
      // artwork url and textfield do not match -> redownload
      movieSetToEdit.setArtworkUrl(textField.getText(), type);
    }
    else if (StringUtils.isEmpty(textField.getText())) {
      // remove the artwork url
      movieSetToEdit.removeArtworkUrl(type);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -6214112833170817002L;

    CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard"));
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
      putValue(NAME, BUNDLE.getString("Button.abortqueue"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.abortqueue"));
      putValue(SMALL_ICON, IconManager.STOP_INV);
      putValue(LARGE_ICON_KEY, IconManager.STOP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  private class NavigateBackAction extends AbstractAction {
    private static final long serialVersionUID = -1652218154720642310L;

    public NavigateBackAction() {
      putValue(NAME, BUNDLE.getString("Button.back"));
      putValue(SMALL_ICON, IconManager.BACK_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      navigateBack = true;
      setVisible(false);
    }
  }

  private class SearchIdAction extends AbstractAction {
    private static final long serialVersionUID = -8980803676368394987L;

    SearchIdAction() {
      putValue(NAME, BUNDLE.getString("movieset.tmdb.find"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.tmdb.desc"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // search for a tmdbId
      try {
        MediaScraper scraper = MediaScraper.getMediaScraperById(MediaMetadata.TMDB, ScraperType.MOVIE);
        IMovieMetadataProvider mp = (IMovieMetadataProvider) scraper.getMediaProvider();

        for (Movie movie : moviesInSet) {
          if (Utils.isValidImdbId(movie.getImdbId()) || movie.getTmdbId() > 0) {
            MovieSearchAndScrapeOptions options = new MovieSearchAndScrapeOptions();
            options.setTmdbId(movie.getTmdbId());
            options.setImdbId(movie.getImdbId());
            options.setLanguage(MovieModuleManager.SETTINGS.getScraperLanguage());

            try {
              MediaMetadata md = mp.getMetadata(options);
              if ((int) md.getId(MediaMetadata.TMDB_SET) > 0) {
                tfTmdbId.setText(String.valueOf(md.getId(MediaMetadata.TMDB_SET)));
                break;
              }
            }
            catch (ScrapeException ex) {
              LOGGER.error("getMetadata", ex);
              MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "MovieSetChooser", "message.scrape.metadatamoviesetfailed",
                  new String[] { ":", ex.getLocalizedMessage() }));
            }
            catch (MissingIdException ex) {
              LOGGER.warn("missing id for scrape");
              MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, "MovieSetChooser", "scraper.error.missingid"));
            }
          }
        }
      }
      catch (Exception ex) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("movieset.tmdb.error"));
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

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  protected BindingGroup initDataBindings() {
    JTableBinding<Movie, List<Movie>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, moviesInSet, tableMovies);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("title");
    jTableBinding.addColumnBinding(movieBeanProperty).setEditable(false);
    //
    BeanProperty<Movie, Integer> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setEditable(false);
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("watched");
    jTableBinding.addColumnBinding(movieBeanProperty_2).setEditable(false).setColumnClass(Boolean.class);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BindingGroup bindingGroup = new BindingGroup();
    //
    bindingGroup.addBinding(jTableBinding);
    return bindingGroup;
  }

  private void setImageSizeAndCreateLink(LinkLabel lblSize, ImageLabel imageLabel, MediaFileType type) {
    createLinkForImage(lblSize, imageLabel);

    // image has been deleted
    if (imageLabel.getOriginalImageSize().width == 0 && imageLabel.getOriginalImageSize().height == 0) {
      lblSize.setText("");
      return;
    }

    Dimension dimension = movieSetToEdit.getArtworkDimension(type);
    if (dimension.width == 0 && dimension.height == 0) {
      lblSize.setText(imageLabel.getOriginalImageSize().width + "x" + imageLabel.getOriginalImageSize().height);
    }
    else {
      lblSize.setText(dimension.width + "x" + dimension.height);
    }
  }
}
