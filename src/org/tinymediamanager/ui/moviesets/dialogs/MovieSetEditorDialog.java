/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieSet;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetEditor.
 * 
 * @author Manuel Laggner
 */
public class MovieSetEditorDialog extends JDialog {

  /** The Constant LOGGER. */
  private static final Logger         LOGGER              = LoggerFactory.getLogger(MovieSetEditorDialog.class);

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE              = ResourceBundle.getBundle("messages", new UTF8Control());     //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID    = -4446433759280691976L;

  /** The movie set to edit. */
  private MovieSet                    movieSetToEdit;

  /** The tf name. */
  private JTextField                  tfName;

  /** The table movies. */
  private JTable                      tableMovies;

  /** The lbl poster. */
  private ImageLabel                  lblPoster;

  /** The lbl fanart. */
  private ImageLabel                  lblFanart;

  /** The tp overview. */
  private JTextPane                   tpOverview;

  /** The movies in set. */
  private List<Movie>                 moviesInSet         = ObservableCollections.observableList(new ArrayList<Movie>());

  /** The removed movies. */
  private List<Movie>                 removedMovies       = new ArrayList<Movie>();

  /** The action remove movie. */
  private final Action                actionRemoveMovie   = new RemoveMovieAction();

  /** The action move movie up. */
  private final Action                actionMoveMovieUp   = new MoveUpAction();

  /** The action move movie down. */
  private final Action                actionMoveMovieDown = new MoveDownAction();

  /** The action ok. */
  private final Action                actionOk            = new OkAction();

  /** The action cancel. */
  private final Action                actionCancel        = new CancelAction();

  /** The action abort. */
  private final Action                actionAbort         = new AbortAction();

  /** The tf tmdb id. */
  private JTextField                  tfTmdbId;

  /** The action search tmdb id. */
  private final Action                actionSearchTmdbId  = new SwingAction();

  /** The artwork providers. */
  private List<IMediaArtworkProvider> artworkProviders    = new ArrayList<IMediaArtworkProvider>();

  /** The continue queue. */
  private boolean                     continueQueue       = true;

  /**
   * Instantiates a new movie set editor.
   * 
   * @param movieSet
   *          the movie set
   * @param inQueue
   *          the in queue
   */
  public MovieSetEditorDialog(MovieSet movieSet, boolean inQueue) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle(BUNDLE.getString("movieset.edit")); //$NON-NLS-1$
    setName("movieSetEditor");
    setBounds(5, 5, 800, 500);
    TmmWindowSaver.loadSettings(this);

    movieSetToEdit = movieSet;
    try {
      artworkProviders.add(new TmdbMetadataProvider());
    }
    catch (Exception e2) {
      LOGGER.warn("error getting IMediaArtworkProvider " + e2.getMessage());
    }

    getContentPane().setLayout(new BorderLayout());

    JPanel panelContent = new JPanel();
    panelContent.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("2dlu"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("300px:grow"), FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("250px:grow"), ColumnSpec.decode("2dlu"), }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        RowSpec.decode("75px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
        FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));
    getContentPane().add(panelContent, BorderLayout.CENTER);

    JLabel lblName = new JLabel(BUNDLE.getString("movieset.title")); //$NON-NLS-1$
    panelContent.add(lblName, "2, 2, right, default");

    tfName = new JTextField();
    panelContent.add(tfName, "4, 2, 3, 1, fill, default");
    tfName.setColumns(10);

    lblPoster = new ImageLabel();
    lblPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
    lblPoster.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int tmdbId = 0;
        try {
          tmdbId = Integer.parseInt(tfTmdbId.getText());
        }
        catch (Exception e1) {
        }
        HashMap<String, Object> ids = new HashMap<String, Object>(movieSetToEdit.getIds());
        ids.put("tmdbId", tmdbId);
        // MovieSetImageChooserDialog dialog = new MovieSetImageChooserDialog(tmdbId, ImageType.POSTER, lblPoster);
        ImageChooserDialog dialog = new ImageChooserDialog(ids, ImageType.POSTER, artworkProviders, lblPoster, null, null);
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
      }
    });
    panelContent.add(lblPoster, "8, 2, 1, 9, fill, fill");

    JLabel lblTmdbid = new JLabel(BUNDLE.getString("metatag.tmdb")); //$NON-NLS-1$
    panelContent.add(lblTmdbid, "2, 4, right, default");

    tfTmdbId = new JTextField();
    panelContent.add(tfTmdbId, "4, 4, fill, default");
    tfTmdbId.setColumns(10);

    JButton btnSearchTmdbId = new JButton("");
    btnSearchTmdbId.setAction(actionSearchTmdbId);
    panelContent.add(btnSearchTmdbId, "6, 4, left, default");

    JLabel lblOverview = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
    panelContent.add(lblOverview, "2, 6, right, top");

    JScrollPane scrollPaneOverview = new JScrollPane();
    panelContent.add(scrollPaneOverview, "4, 6, 3, 1, fill, fill");

    tpOverview = new JTextPane();
    scrollPaneOverview.setViewportView(tpOverview);

    JLabel lblMovies = new JLabel(BUNDLE.getString("tmm.movies")); //$NON-NLS-1$
    panelContent.add(lblMovies, "2, 8, right, top");

    JScrollPane scrollPaneMovies = new JScrollPane();
    panelContent.add(scrollPaneMovies, "4, 8, 3, 9, fill, fill");

    tableMovies = new JTable();
    scrollPaneMovies.setViewportView(tableMovies);

    JButton btnRemoveMovie = new JButton("");
    btnRemoveMovie.setAction(actionRemoveMovie);
    panelContent.add(btnRemoveMovie, "2, 10, right, top");

    JButton btnMoveMovieUp = new JButton("");
    btnMoveMovieUp.setAction(actionMoveMovieUp);
    panelContent.add(btnMoveMovieUp, "2, 12, right, top");

    lblFanart = new ImageLabel();
    lblFanart.setAlternativeText(BUNDLE.getString("image.notfound.fanart")); //$NON-NLS-1$
    lblFanart.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int tmdbId = 0;
        try {
          tmdbId = Integer.parseInt(tfTmdbId.getText());
        }
        catch (Exception e1) {
        }
        HashMap<String, Object> ids = new HashMap<String, Object>(movieSetToEdit.getIds());
        ids.put("tmdbId", tmdbId);
        ImageChooserDialog dialog = new ImageChooserDialog(ids, ImageType.FANART, artworkProviders, lblFanart, null, null);
        // MovieSetImageChooserDialog dialog = new MovieSetImageChooserDialog(tmdbId, ImageType.FANART, lblFanart);
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
      }
    });
    panelContent.add(lblFanart, "8, 12, 1, 5, fill, fill");

    JButton btnMoveMovieDown = new JButton("");
    btnMoveMovieDown.setAction(actionMoveMovieDown);
    panelContent.add(btnMoveMovieDown, "2, 14, right, top");

    /**
     * Button pane
     */
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPane.setLayout(layout);
      {
        JButton btnOk = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        btnOk.setAction(actionOk);
        buttonPane.add(btnOk);
        getRootPane().setDefaultButton(btnOk);

        JButton btnCancel = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        btnCancel.setAction(actionCancel);
        buttonPane.add(btnCancel);

        if (inQueue) {
          JButton abortButton = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          buttonPane.add(abortButton);
          abortButton.setAction(actionAbort);
        }
      }

    }

    {
      tfName.setText(movieSetToEdit.getTitle());
      tfTmdbId.setText(String.valueOf(movieSetToEdit.getTmdbId()));
      tpOverview.setText(movieSetToEdit.getPlot());
      lblPoster.setImageUrl(movieSetToEdit.getPosterUrl());
      moviesInSet.addAll(movieSetToEdit.getMovies());
      lblPoster.setImageUrl(movieSetToEdit.getPosterUrl());
      lblFanart.setImageUrl(movieSetToEdit.getFanartUrl());
    }

    initDataBindings();

    // adjust table columns
    // name column
    tableMovies.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name"));

    // year column
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(35);
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setMinWidth(35);
    tableMovies.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(50);
    tableMovies.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.year"));

    // watched column
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setPreferredWidth(70);
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setMinWidth(70);
    tableMovies.getTableHeader().getColumnModel().getColumn(2).setMaxWidth(85);
    tableMovies.getTableHeader().getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.watched"));
  }

  /**
   * The Class RemoveMovieAction.
   * 
   * @author Manuel Laggner
   */
  private class RemoveMovieAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new removes the movie action.
     */
    public RemoveMovieAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Remove.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.movie.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      if (row > -1) {
        Movie movie = moviesInSet.get(row);
        moviesInSet.remove(row);
        removedMovies.add(movie);
      }
    }
  }

  /**
   * The Class MoveUpAction.
   * 
   * @author Manuel Laggner
   */
  private class MoveUpAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new move up action.
     */
    public MoveUpAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Button_Up.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.movie.moveup")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      if (row > 0) {
        Collections.rotate(moviesInSet.subList(row - 1, row + 1), 1);
        tableMovies.getSelectionModel().setSelectionInterval(row - 1, row - 1);
      }
    }
  }

  /**
   * The Class MoveDownAction.
   * 
   * @author Manuel Laggner
   */
  private class MoveDownAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new move down action.
     */
    public MoveDownAction() {
      putValue(LARGE_ICON_KEY, new ImageIcon(getClass().getResource("/org/tinymediamanager/ui/images/Button_Down.png")));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.movie.movedown")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableMovies.getSelectedRow();
      if (row < moviesInSet.size() - 1) {
        Collections.rotate(moviesInSet.subList(row, row + 2), -1);
        tableMovies.getSelectionModel().setSelectionInterval(row + 1, row + 1);
      }
    }
  }

  /**
   * The Class OkAction.
   * 
   * @author Manuel Laggner
   */
  private class OkAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ok action.
     */
    public OkAction() {
      putValue(NAME, BUNDLE.getString("Button.save")); //$NON-NLS-1$);
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.save")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      movieSetToEdit.setTitle(tfName.getText());
      movieSetToEdit.setPlot(tpOverview.getText());

      // image changes
      if (StringUtils.isNotEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(movieSetToEdit.getPosterUrl())) {
        movieSetToEdit.setPosterUrl(lblPoster.getImageUrl());
      }
      if (StringUtils.isNotEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(movieSetToEdit.getFanartUrl())) {
        movieSetToEdit.setFanartUrl(lblFanart.getImageUrl());
      }

      // delete movies
      for (int i = movieSetToEdit.getMovies().size() - 1; i >= 0; i--) {
        Movie movie = movieSetToEdit.getMovies().get(i);
        if (!moviesInSet.contains(movie)) {
          movie.setMovieSet(null);
          movieSetToEdit.removeMovie(movie);
          movie.writeNFO();
        }
      }

      // sort movies in the right order
      for (int i = 0; i < moviesInSet.size(); i++) {
        Movie movie = moviesInSet.get(i);
        movie.setSortTitle(movieSetToEdit.getTitle() + (i + 1));
      }

      // remove removed movies
      for (Movie movie : removedMovies) {
        movie.removeFromMovieSet();
        movieSetToEdit.removeMovie(movie);
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
      catch (Exception e1) {
      }
      movieSetToEdit.setTmdbId(tmdbId);
      movieSetToEdit.saveToDb();

      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class CancelAction.
   * 
   * @author Manuel Laggner
   */
  private class CancelAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new cancel action.
     */
    public CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class AbortAction.
   * 
   * @author Manuel Laggner
   */
  private class AbortAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new cancel action.
     */
    public AbortAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
      dispose();
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<Movie, List<Movie>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, moviesInSet, tableMovies);
    //
    BeanProperty<Movie, String> movieBeanProperty = BeanProperty.create("title");
    jTableBinding.addColumnBinding(movieBeanProperty).setEditable(false); //$NON-NLS-1$
    //
    BeanProperty<Movie, String> movieBeanProperty_1 = BeanProperty.create("year");
    jTableBinding.addColumnBinding(movieBeanProperty_1).setEditable(false); //$NON-NLS-1$
    //
    BeanProperty<Movie, Boolean> movieBeanProperty_2 = BeanProperty.create("watched");
    jTableBinding.addColumnBinding(movieBeanProperty_2).setEditable(false).setColumnClass(Boolean.class); //$NON-NLS-1$
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
  }

  /**
   * The Class SwingAction.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action.
     */
    public SwingAction() {
      putValue(NAME, BUNDLE.getString("movieset.tmdb.find")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("movieset.tmdb.desc")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // search for a tmdbId
      try {
        TmdbMetadataProvider tmdb = new TmdbMetadataProvider();
        for (Movie movie : moviesInSet) {
          MediaScrapeOptions options = new MediaScrapeOptions();
          if (Utils.isValidImdbId(movie.getImdbId()) || movie.getTmdbId() > 0) {
            options.setTmdbId(movie.getTmdbId());
            options.setImdbId(movie.getImdbId());
            MediaMetadata md = tmdb.getMetadata(options);
            if (md.getTmdbIdSet() > 0) {
              tfTmdbId.setText(String.valueOf(md.getTmdbIdSet()));
              break;
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
    setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }
}
