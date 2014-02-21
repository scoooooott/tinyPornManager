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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieRenamerPreview;
import org.tinymediamanager.core.movie.MovieRenamerPreviewContainer;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.ZebraJTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class RenamerPreviewDialog. generate a preview which movies have to be renamed.
 * 
 * @author Manuel Laggner
 */
public class RenamerPreviewDialog extends JDialog {
  private static final long                                    serialVersionUID = -8162631708278089277L;
  private static final ResourceBundle                          BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private EventList<MovieRenamerPreviewContainer>              results;
  private SortedList<MovieRenamerPreviewContainer>             sortedResults;
  private DefaultEventTableModel<MovieRenamerPreviewContainer> movieTableModel;
  private ResultSelectionModel                                 resultSelectionModel;
  private EventList<MediaFile>                                 oldMediaFileEventList;
  private DefaultEventTableModel<MediaFile>                    oldMediaFileTableModel;
  private EventList<MediaFile>                                 newMediaFileEventList;
  private DefaultEventTableModel<MediaFile>                    newMediaFileTableModel;

  /** UI components */
  private JTable                                               tableMovies;
  private JLabel                                               lblTitle;
  private JLabel                                               lblFolderOld;
  private JLabel                                               lblFolderNew;
  private JTable                                               tableMediaFilesNew;
  private JTable                                               tableMediaFilesOld;

  public RenamerPreviewDialog(final List<Movie> selectedMovies) {
    super((Frame) null, BUNDLE.getString("movie.renamerpreview"), true); //$NON-NLS-1$
    setIconImage(Globals.logo);
    setName("movieRenamerPreview");
    setBounds(5, 5, 950, 700);
    setLocationRelativeTo(MainWindow.getActiveInstance());
    TmmWindowSaver.loadSettings(this);

    results = GlazedLists.threadSafeList(new BasicEventList<MovieRenamerPreviewContainer>());
    sortedResults = new SortedList<MovieRenamerPreviewContainer>(GlazedListsSwing.swingThreadProxyList(results), new ResultComparator());
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
          FormFactory.RELATED_GAP_ROWSPEC, }));
      {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.2);
        panelContent.add(splitPane, "2, 2, fill, fill");
        {
          movieTableModel = new DefaultEventTableModel<MovieRenamerPreviewContainer>(sortedResults, new ResultTableFormat());
          tableMovies = new ZebraJTable(movieTableModel);

          DefaultEventSelectionModel<MovieRenamerPreviewContainer> tableSelectionModel = new DefaultEventSelectionModel<MovieRenamerPreviewContainer>(
              results);
          resultSelectionModel = new ResultSelectionModel();
          tableSelectionModel.addListSelectionListener(resultSelectionModel);
          resultSelectionModel.selectedResults = tableSelectionModel.getSelected();
          tableMovies.setSelectionModel(tableSelectionModel);

          movieTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent arg0) {
              // select first movie if nothing is selected
              ListSelectionModel selectionModel = tableMovies.getSelectionModel();
              if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
                selectionModel.setSelectionInterval(0, 0);
              }
              if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() == 0) {
                resultSelectionModel.setSelectedResult(null);
              }
            }
          });

          JScrollPane scrollPaneMovies = ZebraJTable.createStripedJScrollPane(tableMovies);
          scrollPaneMovies.setViewportView(tableMovies);
          splitPane.setLeftComponent(scrollPaneMovies);

        }
        {
          JPanel panelDetails = new JPanel();
          splitPane.setRightComponent(panelDetails);
          panelDetails.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
              FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
              FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
              FormFactory.RELATED_GAP_ROWSPEC, }));
          {
            lblTitle = new JLabel("");
            lblTitle.setFont(lblTitle.getFont().deriveFont(16f).deriveFont(Font.BOLD));
            panelDetails.add(lblTitle, "2, 2, 3, 1");
          }
          {
            JLabel lblFolderOldT = new JLabel("Folder old");
            panelDetails.add(lblFolderOldT, "2, 4");
          }
          {
            lblFolderOld = new JLabel("");
            panelDetails.add(lblFolderOld, "4, 4");
          }
          {
            JLabel lblFolderNewT = new JLabel("Folder new");
            panelDetails.add(lblFolderNewT, "2, 6");
          }
          {
            lblFolderNew = new JLabel("");
            panelDetails.add(lblFolderNew, "4, 6");
          }
          {
            JPanel panelMediaFiles = new JPanel();
            panelDetails.add(panelMediaFiles, "2, 8, 3, 1, fill, fill");
            panelMediaFiles.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), }, new RowSpec[] { RowSpec.decode("fill:default:grow"), }));
            {
              JScrollPane scrollPaneMediaFilesOld = new JScrollPane();
              panelMediaFiles.add(scrollPaneMediaFilesOld, "1, 1, fill, fill");
              {
                oldMediaFileEventList = GlazedLists.eventList(new ArrayList<MediaFile>());
                oldMediaFileTableModel = new DefaultEventTableModel<MediaFile>(GlazedListsSwing.swingThreadProxyList(oldMediaFileEventList),
                    new MediaFileTableFormat());
                tableMediaFilesOld = new ZebraJTable(oldMediaFileTableModel);
                scrollPaneMediaFilesOld.setViewportView(tableMediaFilesOld);
              }
            }
            {
              JScrollPane scrollPaneMediaFilesNew = new JScrollPane();
              panelMediaFiles.add(scrollPaneMediaFilesNew, "3, 1, fill, fill");
              {
                newMediaFileEventList = GlazedLists.eventList(new ArrayList<MediaFile>());
                newMediaFileTableModel = new DefaultEventTableModel<MediaFile>(GlazedListsSwing.swingThreadProxyList(newMediaFileEventList),
                    new MediaFileTableFormat());
                tableMediaFilesNew = new ZebraJTable(newMediaFileTableModel);
                scrollPaneMediaFilesNew.setViewportView(tableMediaFilesNew);
              }
            }
          }
        }
      }
    }
    {
      JPanel panelButtons = new JPanel();
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      panelButtons.setLayout(layout);
      panelButtons.setBorder(new EmptyBorder(4, 4, 4, 4));
      getContentPane().add(panelButtons, BorderLayout.SOUTH);
      {
        JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
        btnClose.setIcon(IconManager.APPLY);
        btnClose.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
            dispose();
          }
        });
        panelButtons.add(btnClose);
      }
    }

    // start calculation of the preview
    MoviePreviewWorker worker = new MoviePreviewWorker(selectedMovies);
    worker.execute();
  }

  /**********************************************************************
   * helper classes
   *********************************************************************/
  private class ResultTableFormat implements TableFormat<MovieRenamerPreviewContainer> {
    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.movie"); //$NON-NLS-1$
      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MovieRenamerPreviewContainer result, int column) {
      switch (column) {
        case 0:
          return result.getMovie().getTitleSortable();
      }

      throw new IllegalStateException();
    }
  }

  private class MediaFileTableFormat implements TableFormat<MediaFile> {
    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.filename"); //$NON-NLS-1$
      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MediaFile mediaFile, int column) {
      switch (column) {
        case 0:
          return mediaFile.getFilename();
      }

      throw new IllegalStateException();
    }
  }

  private class ResultComparator implements Comparator<MovieRenamerPreviewContainer> {
    private RuleBasedCollator stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

    @Override
    public int compare(MovieRenamerPreviewContainer result1, MovieRenamerPreviewContainer result2) {
      if (stringCollator != null) {
        return stringCollator.compare(result1.getMovie().getTitleSortable().toLowerCase(), result2.getMovie().getTitleSortable().toLowerCase());
      }
      return result1.getMovie().getTitleSortable().toLowerCase().compareTo(result2.getMovie().getTitleSortable().toLowerCase());
    }
  }

  private class MoviePreviewWorker extends SwingWorker<Void, Void> {
    private List<Movie> moviesToProcess;

    private MoviePreviewWorker(List<Movie> movies) {
      this.moviesToProcess = movies;
    }

    @Override
    protected Void doInBackground() throws Exception {
      for (Movie movie : moviesToProcess) {
        MovieRenamerPreviewContainer container = MovieRenamerPreview.renameMovie(movie);
        results.add(container);
      }
      return null;
    }
  }

  private class ResultSelectionModel extends AbstractModelObject implements ListSelectionListener {
    private MovieRenamerPreviewContainer       selectedResult;
    private List<MovieRenamerPreviewContainer> selectedResults;
    private MovieRenamerPreviewContainer       emptyResult;

    public ResultSelectionModel() {
      emptyResult = new MovieRenamerPreviewContainer(new Movie());
    }

    public void setSelectedResult(MovieRenamerPreviewContainer newValue) {
      selectedResult = newValue;

      lblTitle.setText(selectedResult.getMovie().getTitleSortable());
      lblFolderOld.setText(selectedResult.getOldPath());
      lblFolderNew.setText(selectedResult.getNewPath());

      // set Mfs
      try {
        oldMediaFileEventList.getReadWriteLock().writeLock().lock();
        oldMediaFileEventList.clear();
        oldMediaFileEventList.addAll(selectedResult.getOldMediaFiles());
      }
      catch (Exception e) {
      }
      finally {
        oldMediaFileEventList.getReadWriteLock().writeLock().unlock();
      }

      try {
        newMediaFileEventList.getReadWriteLock().writeLock().lock();
        newMediaFileEventList.clear();
        newMediaFileEventList.addAll(selectedResult.getNewMediaFiles());
      }
      catch (Exception e) {
      }
      finally {
        newMediaFileEventList.getReadWriteLock().writeLock().unlock();
      }
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {
      if (arg0.getValueIsAdjusting()) {
        return;
      }

      // display first selected result
      if (!selectedResults.isEmpty() && selectedResult != selectedResults.get(0)) {
        setSelectedResult(selectedResults.get(0));
      }

      // display empty result
      if (selectedResults.isEmpty()) {
        setSelectedResult(emptyResult);
      }
    }
  }
}