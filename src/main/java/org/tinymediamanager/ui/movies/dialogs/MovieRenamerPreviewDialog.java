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
package org.tinymediamanager.ui.movies.dialogs;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieComparator;
import org.tinymediamanager.core.movie.MovieRenamerPreview;
import org.tinymediamanager.core.movie.MovieRenamerPreviewContainer;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.tasks.MovieRenameTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.components.TmmSplitPane;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The class MovieRenamerPreviewDialog. generate a preview which movies have to be renamed.
 * 
 * @author Manuel Laggner
 */
public class MovieRenamerPreviewDialog extends TmmDialog {
  private static final long                       serialVersionUID = -8162631708278089277L;

  private EventList<MovieRenamerPreviewContainer> results;
  private ResultSelectionModel                    resultSelectionModel;
  private EventList<MediaFileContainer>           oldMediaFileEventList;
  private EventList<MediaFileContainer>           newMediaFileEventList;

  /** UI components */
  private TmmTable                                tableMovies;
  private JLabel                                  lblTitle;
  private JLabel                                  lblDatasource;
  private JLabel                                  lblFolderOld;
  private JLabel                                  lblFolderNew;

  public MovieRenamerPreviewDialog(final List<Movie> selectedMovies) {
    super(BUNDLE.getString("movie.renamerpreview"), "movieRenamerPreview");

    oldMediaFileEventList = GlazedLists.eventList(new ArrayList<>());
    newMediaFileEventList = GlazedLists.eventList(new ArrayList<>());

    results = GlazedListsSwing.swingThreadProxyList(GlazedLists.threadSafeList(new BasicEventList<>()));
    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[950lp,grow]", "[600lp,grow]"));
      {
        JSplitPane splitPane = new TmmSplitPane();
        splitPane.setResizeWeight(0.4);
        panelContent.add(splitPane, "cell 0 0,grow");
        {
          DefaultEventTableModel<MovieRenamerPreviewContainer> movieTableModel = new DefaultEventTableModel<>(
              GlazedListsSwing.swingThreadProxyList(results), new ResultTableFormat());
          tableMovies = new TmmTable(movieTableModel);

          DefaultEventSelectionModel<MovieRenamerPreviewContainer> tableSelectionModel = new DefaultEventSelectionModel<>(results);
          resultSelectionModel = new ResultSelectionModel();
          tableSelectionModel.addListSelectionListener(resultSelectionModel);
          resultSelectionModel.selectedResults = tableSelectionModel.getSelected();
          tableMovies.setSelectionModel(tableSelectionModel);

          movieTableModel.addTableModelListener(arg0 -> {
            // select first movie if nothing is selected
            ListSelectionModel selectionModel = tableMovies.getSelectionModel();
            if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() > 0) {
              selectionModel.setSelectionInterval(0, 0);
            }
            if (selectionModel.isSelectionEmpty() && movieTableModel.getRowCount() == 0) {
              resultSelectionModel.setSelectedResult(null);
            }
          });

          JScrollPane scrollPaneMovies = new JScrollPane(tableMovies);
          tableMovies.configureScrollPane(scrollPaneMovies);
          splitPane.setLeftComponent(scrollPaneMovies);
        }
        {
          JPanel panelDetails = new JPanel();
          splitPane.setRightComponent(panelDetails);
          panelDetails.setLayout(new MigLayout("", "[][][grow]", "[][][][][][][][grow]"));
          {
            lblTitle = new JLabel("");
            TmmFontHelper.changeFont(lblTitle, 1.33, Font.BOLD);
            panelDetails.add(lblTitle, "cell 0 0 3 1,growx");
          }
          {
            JLabel lblDatasourceT = new TmmLabel(BUNDLE.getString("metatag.datasource"));
            panelDetails.add(lblDatasourceT, "cell 0 2");

            lblDatasource = new JLabel("");
            panelDetails.add(lblDatasource, "cell 2 2,growx,aligny center");
          }
          {
            JLabel lblFolderOldT = new TmmLabel(BUNDLE.getString("renamer.oldfolder"));
            panelDetails.add(lblFolderOldT, "cell 0 4");

            lblFolderOld = new JLabel("");
            panelDetails.add(lblFolderOld, "cell 2 4,growx,aligny center");
          }
          {
            JLabel lblFolderNewT = new TmmLabel(BUNDLE.getString("renamer.newfolder"));
            panelDetails.add(lblFolderNewT, "cell 0 5");

            lblFolderNew = new JLabel("");
            panelDetails.add(lblFolderNew, "cell 2 5,growx,aligny center");
          }
          {
            JPanel panelMediaFiles = new JPanel();
            panelDetails.add(panelMediaFiles, "cell 0 7 3 1,grow");
            panelMediaFiles.setLayout(new MigLayout("", "[grow][grow]", "[15px][grow]"));
            {
              JLabel lblOldfilesT = new TmmLabel(BUNDLE.getString("renamer.oldfiles"));
              panelMediaFiles.add(lblOldfilesT, "cell 0 0,alignx center");

              JLabel lblNewfilesT = new TmmLabel(BUNDLE.getString("renamer.newfiles"));
              panelMediaFiles.add(lblNewfilesT, "cell 1 0,alignx center");
            }
            {
              DefaultEventTableModel<MediaFileContainer> oldMediaFileTableModel = new DefaultEventTableModel<>(
                  GlazedListsSwing.swingThreadProxyList(oldMediaFileEventList), new MediaFileTableFormat());
              TmmTable tableMediaFilesOld = new TmmTable(oldMediaFileTableModel);
              JScrollPane scrollPaneMediaFilesOld = new JScrollPane(tableMediaFilesOld);
              tableMediaFilesOld.configureScrollPane(scrollPaneMediaFilesOld);
              panelMediaFiles.add(scrollPaneMediaFilesOld, "cell 0 1,grow");
              tableMediaFilesOld.getColumnModel().getColumn(0).setMaxWidth(40);
            }
            {

              DefaultEventTableModel<MediaFileContainer> newMediaFileTableModel = new DefaultEventTableModel<>(
                  GlazedListsSwing.swingThreadProxyList(newMediaFileEventList), new MediaFileTableFormat());
              TmmTable tableMediaFilesNew = new TmmTable(newMediaFileTableModel);
              JScrollPane scrollPaneMediaFilesNew = new JScrollPane(tableMediaFilesNew);
              tableMediaFilesNew.configureScrollPane(scrollPaneMediaFilesNew);
              panelMediaFiles.add(scrollPaneMediaFilesNew, "cell 1 1,grow");
              tableMediaFilesNew.getColumnModel().getColumn(0).setMaxWidth(40);
            }
          }
        }
      }
    }
    {
      JButton btnRename = new JButton(BUNDLE.getString("Button.rename"));
      btnRename.setToolTipText(BUNDLE.getString("movie.rename"));
      btnRename.addActionListener(arg0 -> {
        List<Movie> selectedMovies1 = new ArrayList<>();
        List<MovieRenamerPreviewContainer> selectedResults = new ArrayList<>(resultSelectionModel.selectedResults);
        for (MovieRenamerPreviewContainer result : selectedResults) {
          selectedMovies1.add(result.getMovie());
        }

        // rename
        TmmThreadPool renameTask = new MovieRenameTask(selectedMovies1);
        TmmTaskManager.getInstance().addMainTask(renameTask);
        results.removeAll(selectedResults);
      });
      addButton(btnRename);

      JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
      btnClose.addActionListener(arg0 -> setVisible(false));
      addDefaultButton(btnClose);
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
          return BUNDLE.getString("metatag.movie");
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

  private class MediaFileTableFormat implements AdvancedTableFormat<MediaFileContainer> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "";

        case 1:
          return BUNDLE.getString("metatag.filename");
      }

      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(MediaFileContainer mediaFileContainer, int column) {
      switch (column) {
        case 0:
          return mediaFileContainer.icon;

        case 1:
          return mediaFileContainer.mediaFile.getFilename();
      }

      throw new IllegalStateException();
    }

    @Override
    public Class<?> getColumnClass(int column) {
      switch (column) {
        case 0:
          return ImageIcon.class;

        case 1:
          return String.class;
      }
      throw new IllegalStateException();
    }

    @Override
    public Comparator<MediaFileContainer> getColumnComparator(int column) {
      return null;
    }
  }

  private class MoviePreviewWorker extends SwingWorker<Void, Void> {
    private List<Movie> moviesToProcess;

    private MoviePreviewWorker(List<Movie> movies) {
      this.moviesToProcess = new ArrayList<>(movies);
    }

    @Override
    protected Void doInBackground() {
      // sort movies
      moviesToProcess.sort(new MovieComparator());
      // rename them
      for (Movie movie : moviesToProcess) {
        MovieRenamerPreviewContainer container = MovieRenamerPreview.renameMovie(movie);
        if (container.isNeedsRename()) {
          results.add(container);
        }
      }
      return null;
    }
  }

  private class ResultSelectionModel extends AbstractModelObject implements ListSelectionListener {
    private MovieRenamerPreviewContainer       selectedResult;
    private List<MovieRenamerPreviewContainer> selectedResults;
    private MovieRenamerPreviewContainer       emptyResult;

    ResultSelectionModel() {
      emptyResult = new MovieRenamerPreviewContainer(new Movie());
    }

    synchronized void setSelectedResult(MovieRenamerPreviewContainer newValue) {
      if (newValue == null) {
        selectedResult = emptyResult;
      }
      else {
        selectedResult = newValue;
      }

      lblTitle.setText(selectedResult.getMovie().getTitleSortable());
      lblDatasource.setText(selectedResult.getMovie().getDataSource());

      // the empty result does not have any valid Path
      if (selectedResult != emptyResult) {
        lblFolderOld.setText(selectedResult.getOldPath().toString());
        lblFolderNew.setText(selectedResult.getNewPath().toString());
      }
      else {
        lblFolderOld.setText("");
        lblFolderNew.setText("");
      }

      // set Mfs
      try {
        oldMediaFileEventList.getReadWriteLock().writeLock().lock();
        oldMediaFileEventList.clear();
        for (MediaFile mf : selectedResult.getOldMediaFiles()) {
          boolean found = false;
          MediaFileContainer container = new MediaFileContainer();
          container.mediaFile = mf;

          for (MediaFile mf2 : selectedResult.getNewMediaFiles()) {
            if (mf2 != null && mf.getFilename().equals(mf2.getFilename())) {
              found = true;
              break;
            }
          }

          if (!found) {
            container.icon = IconManager.REMOVE;
          }
          oldMediaFileEventList.add(container);
        }

        newMediaFileEventList.getReadWriteLock().writeLock().lock();
        newMediaFileEventList.clear();
        for (MediaFile mf : selectedResult.getNewMediaFiles()) {
          boolean found = false;
          MediaFileContainer container = new MediaFileContainer();
          container.mediaFile = mf;

          for (MediaFile mf2 : selectedResult.getOldMediaFiles()) {
            if (mf.getFilename().equals(mf2.getFilename())) {
              found = true;
              break;
            }
          }

          if (!found) {
            container.icon = IconManager.ADD;
          }
          newMediaFileEventList.add(container);
        }
      }
      catch (Exception ignored) {
      }
      finally {
        oldMediaFileEventList.getReadWriteLock().writeLock().unlock();
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

  private class MediaFileContainer {
    ImageIcon icon = null;
    MediaFile mediaFile;
  }
}
