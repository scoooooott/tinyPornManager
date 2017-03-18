/*
 * Copyright 2012 - 2017 Manuel Laggner
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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.japura.gui.CheckComboBox;
import org.japura.gui.model.ListCheckModel;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieSubtitleDownloadTask;
import org.tinymediamanager.core.threading.DownloadTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.SubtitleSearchOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaSubtitleProvider;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.MediaScraperCheckComboBox;
import org.tinymediamanager.ui.dialogs.TmmDialog;
import org.tinymediamanager.ui.movies.MovieSubtitleChooserModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

/**
 * This dialog is used to show a chooser for subtitles found with the subtitle scrapers
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleChooserDialog extends TmmDialog {
  private static final long                                 serialVersionUID   = -3104541519073924724L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle                       BUNDLE             = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final MovieList                                   movieList          = MovieList.getInstance();
  private final Movie                                       movieToScrape;
  private final MediaFile                                   fileToScrape;
  private SearchTask                                        activeSearchTask   = null;

  private EventList<MovieSubtitleChooserModel>              subtitleEventList  = null;
  private DefaultEventTableModel<MovieSubtitleChooserModel> subtitleTableModel = null;

  private final boolean                                     inQueue;
  private boolean                                           continueQueue      = true;

  // UI components
  private JTable                                            tableSubs;
  private JTextField                                        tfSearchQuery;
  private JComboBox<MediaLanguages>                         cbLanguage;
  private CheckComboBox                                     cbScraper;
  private JLabel                                            lblProgressAction;
  private JProgressBar                                      progressBar;

  public MovieSubtitleChooserDialog(Movie movie, MediaFile mediaFile, boolean inQueue) {
    super(BUNDLE.getString("moviesubtitlechooser.search"), "movieSubtitleChooser"); //$NON-NLS-1$
    setBounds(5, 5, 712, 429);

    this.movieToScrape = movie;
    this.fileToScrape = mediaFile;
    this.inQueue = inQueue;

    subtitleEventList = GlazedLists.threadSafeList(
        new ObservableElementList<>(new BasicEventList<MovieSubtitleChooserModel>(), GlazedLists.beanConnector(MovieSubtitleChooserModel.class)));
    subtitleTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(subtitleEventList), new SubtitleTableFormat());

    initComponents();

    // initializations
    LinkListener linkListener = new LinkListener();
    tableSubs.addMouseListener(linkListener);
    tableSubs.addMouseMotionListener(linkListener);
    tableSubs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    TableColumnResizer.adjustColumnPreferredWidths(tableSubs, 7);

    cbScraper.setTextFor(CheckComboBox.NONE, BUNDLE.getString("scraper.selected.none")); //$NON-NLS-1$
    cbScraper.setTextFor(CheckComboBox.MULTIPLE, BUNDLE.getString("scraper.selected.multiple")); //$NON-NLS-1$
    cbScraper.setTextFor(CheckComboBox.ALL, BUNDLE.getString("scraper.selected.all")); //$NON-NLS-1$

    ListCheckModel model = cbScraper.getModel();
    for (MediaScraper scraper : movieList.getAvailableSubtitleScrapers()) {
      model.addElement(scraper);

      if (MovieModuleManager.MOVIE_SETTINGS.getMovieSubtitleScrapers().contains(scraper.getId())) {
        model.addCheck(scraper);
      }
    }

    for (MediaLanguages language : MediaLanguages.values()) {
      cbLanguage.addItem(language);
      if (language == MovieModuleManager.MOVIE_SETTINGS.getSubtitleScraperLanguage()) {
        cbLanguage.setSelectedItem(language);
      }
    }

    // start initial search
    searchSubtitle(fileToScrape.getFileAsPath().toFile(), movieToScrape.getImdbId(), tfSearchQuery.getText());
  }

  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());

    final JPanel panelContent = new JPanel();
    getContentPane().add(panelContent, BorderLayout.CENTER);
    panelContent.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("100dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("120dlu:grow"),
            FormSpecs.RELATED_GAP_ROWSPEC, }));

    final JLabel lblMovieTitle = new JLabel(movieToScrape.getTitle());
    TmmFontHelper.changeFont(lblMovieTitle, 1.33, Font.BOLD);
    panelContent.add(lblMovieTitle, "2, 2, 9, 1");

    final JLabel lblMediaFileNameT = new JLabel(BUNDLE.getString("metatag.filename")); //$NON-NLS-1$
    panelContent.add(lblMediaFileNameT, "2, 4, right, default");

    final JLabel lblMediaFileName = new JLabel(fileToScrape.getFilename());
    panelContent.add(lblMediaFileName, "4, 4, 7, 1");

    final JLabel lblRuntimeT = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
    panelContent.add(lblRuntimeT, "2, 6, right, default");

    final JLabel lblRuntime = new JLabel(fileToScrape.getDurationHHMMSS());
    panelContent.add(lblRuntime, "4, 6");

    final JLabel lblImdbIdT = new JLabel(BUNDLE.getString("metatag.imdb")); //$NON-NLS-1$
    panelContent.add(lblImdbIdT, "6, 6, right, default");

    final JLabel lblImdbId = new JLabel(movieToScrape.getImdbId());
    panelContent.add(lblImdbId, "8, 6");

    final JLabel lblScraperT = new JLabel(BUNDLE.getString("scraper")); //$NON-NLS-1$
    panelContent.add(lblScraperT, "2, 8, right, default");

    cbScraper = new MediaScraperCheckComboBox();
    panelContent.add(cbScraper, "4, 8, fill, default");

    tfSearchQuery = new JTextField(movieToScrape.getTitle());
    panelContent.add(tfSearchQuery, "6, 8, 3, 1, fill, default");
    tfSearchQuery.setColumns(10);

    final JButton btnSearch = new JButton(BUNDLE.getString("Button.search")); //$NON-NLS-1$
    btnSearch.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchSubtitle(null, "", tfSearchQuery.getText());
      }
    });
    panelContent.add(btnSearch, "10, 8");

    final JLabel lblLanguageT = new JLabel(BUNDLE.getString("metatag.language")); //$NON-NLS-1$
    panelContent.add(lblLanguageT, "2, 10, right, default");

    cbLanguage = new JComboBox<>();
    cbLanguage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        searchSubtitle(null, "", tfSearchQuery.getText());
      }
    });
    panelContent.add(cbLanguage, "4, 10, fill, default");

    final JScrollPane scrollPaneSubs = new JScrollPane();
    panelContent.add(scrollPaneSubs, "2, 12, 9, 1, fill, fill");

    tableSubs = new JTable(subtitleTableModel);
    tableSubs.setDefaultRenderer(ImageIcon.class, new Renderer());
    scrollPaneSubs.setViewportView(tableSubs);

    {
      JPanel panelBottom = new JPanel();
      getContentPane().add(panelBottom, BorderLayout.SOUTH);
      panelBottom.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(82dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("default:grow"), FormFactory.DEFAULT_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC }));

      progressBar = new JProgressBar();
      panelBottom.add(progressBar, "2, 2");

      lblProgressAction = new JLabel("");
      panelBottom.add(lblProgressAction, "4, 2");

      {
        final JPanel panelButtons = new JPanel();
        EqualsLayout layout = new EqualsLayout(5);
        layout.setMinWidth(100);
        panelButtons.setLayout(layout);
        panelBottom.add(panelButtons, "5, 2, fill, fill");

        JButton btnDone = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
        btnDone.setIcon(IconManager.APPLY);
        btnDone.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        });
        panelButtons.add(btnDone);

        if (inQueue) {
          JButton btnAbortQueue = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
          btnAbortQueue.setIcon(IconManager.PROCESS_STOP);
          btnAbortQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              continueQueue = false;
              setVisible(false);
            }
          });
          panelButtons.add(btnAbortQueue);
        }
      }
    }
  }

  private void searchSubtitle(File file, String imdbId, String searchTerm) {
    if (activeSearchTask != null && !activeSearchTask.isDone()) {
      activeSearchTask.cancel();
    }

    // scrapers
    List<MediaScraper> scrapers = new ArrayList<>();
    ListCheckModel model = cbScraper.getModel();
    for (Object checked : model.getCheckeds()) {
      if (checked != null && checked instanceof MediaScraper) {
        scrapers.add((MediaScraper) checked);
      }
    }

    activeSearchTask = new SearchTask(file, imdbId, searchTerm, scrapers);
    activeSearchTask.execute();
  }

  private void startProgressBar(final String description) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        lblProgressAction.setText(description);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
      }
    });
  }

  private void stopProgressBar() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        lblProgressAction.setText("");
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
      }
    });
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   *
   * @return true, if successful
   */
  public boolean showDialog() {
    // pack();
    // setLocationRelativeTo(MainWindow.getActiveInstance());
    setVisible(true);
    return continueQueue;
  }

  private class SearchTask extends SwingWorker<Void, Void> {
    private File                       file;
    private String                     searchTerm;
    private String                     imdbId;
    private List<SubtitleSearchResult> searchResults;
    private MediaLanguages             language;
    private List<MediaScraper>         scrapers;
    boolean                            cancel;

    public SearchTask(File file, String imdbId, String searchTerm, List<MediaScraper> scrapers) {
      this.file = file;
      this.searchTerm = searchTerm;
      this.imdbId = imdbId;
      this.language = (MediaLanguages) cbLanguage.getSelectedItem();
      this.searchResults = new ArrayList<>();
      this.scrapers = scrapers;
      this.cancel = false;
    }

    @Override
    public Void doInBackground() {
      startProgressBar(BUNDLE.getString("chooser.searchingfor") + " " + searchTerm); //$NON-NLS-1$
      for (MediaScraper scraper : scrapers) {
        try {
          IMediaSubtitleProvider subtitleProvider = (IMediaSubtitleProvider) scraper.getMediaProvider();
          SubtitleSearchOptions options = new SubtitleSearchOptions(file, searchTerm);
          options.setImdbId(imdbId);
          options.setLanguage(LocaleUtils.toLocale(language.name()));
          searchResults.addAll(subtitleProvider.search(options));
        }
        catch (Exception e) {
        }
      }

      Collections.sort(searchResults);
      Collections.reverse(searchResults);

      return null;
    }

    public void cancel() {
      cancel = true;
    }

    @Override
    public void done() {
      if (!cancel) {
        subtitleEventList.clear();
        if (searchResults == null || searchResults.size() == 0) {
          // display empty result
          subtitleEventList.add(MovieSubtitleChooserModel.EMPTY_RESULT);
        }
        else {
          for (SubtitleSearchResult result : searchResults) {
            subtitleEventList.add(new MovieSubtitleChooserModel(result, language));
            // get metadataProvider from searchresult
          }
        }
        if (!subtitleEventList.isEmpty()) {
          tableSubs.setRowSelectionInterval(0, 0); // select first row
        }
        TableColumnResizer.adjustColumnPreferredWidths(tableSubs, 15);
      }
      stopProgressBar();
    }
  }

  private static class SubtitleTableFormat implements AdvancedTableFormat<MovieSubtitleChooserModel> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "";

        case 1:
          return BUNDLE.getString("metatag.title"); //$NON-NLS-1$

        case 2:
          return BUNDLE.getString("metatag.releasename"); //$NON-NLS-1$

      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MovieSubtitleChooserModel model, int column) {
      switch (column) {
        case 0:
          return IconManager.DOWNLOAD;

        case 1:
          return model.getName();

        case 2:
          return model.getReleaseName();
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
          return ImageIcon.class;

        case 1:
        case 2:
          return String.class;
      }

      throw new IllegalStateException();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Comparator getColumnComparator(int arg0) {
      return null;
    }
  }

  private class Renderer extends DefaultTableCellRenderer {

    private final JLabel downloadLabel;

    public Renderer() {
      downloadLabel = new JLabel(BUNDLE.getString("Button.download"), IconManager.DOWNLOAD, SwingConstants.CENTER); //$NON-NLS-1$
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (value == IconManager.DOWNLOAD) {
        return downloadLabel;
      }
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }

  private class LinkListener implements MouseListener, MouseMotionListener {
    @Override
    public void mouseClicked(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));

      // click on the download button
      if (col == 0) {
        row = table.convertRowIndexToModel(row);
        MovieSubtitleChooserModel model = subtitleEventList.get(row);

        if (StringUtils.isNotBlank(model.getDownloadUrl())) {
          // the right language tag from the renamer settings
          String lang = LanguageStyle.getLanguageCodeForStyle(model.getLanguage().name(),
              MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerLanguageStyle());
          if (StringUtils.isBlank(lang)) {
            lang = model.getLanguage().name();
          }
          DownloadTask task = new MovieSubtitleDownloadTask(model.getDownloadUrl(), fileToScrape.getFileAsPath(), lang, movieToScrape);
          TmmTaskManager.getInstance().addDownloadTask(task);
        }
      }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col == 0) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mouseExited(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      JTable table = (JTable) e.getSource();
      int col = table.columnAtPoint(new Point(e.getX(), e.getY()));
      if (col != 0 && table.getCursor().getType() == Cursor.HAND_CURSOR) {
        table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      if (col == 0 && table.getCursor().getType() == Cursor.DEFAULT_CURSOR) {
        table.setCursor(new Cursor(Cursor.HAND_CURSOR));
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
    }
  }
}
