/*
 * Copyright 2012 - 2019 Manuel Laggner
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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * The dialog {@link MovieCleanUpUnwantedFilesDialog} displays found _unknown files_ for selected movies
 * 
 * @author Wolfgang Janes
 */
public class MovieCleanUpUnwantedFilesDialog extends TmmDialog {
  private final static Logger      LOGGER = LoggerFactory.getLogger(MovieCleanUpUnwantedFilesDialog.class);

  private EventList<FileContainer> results;

  private TmmTable                 table;
  private JButton                  btnClean;
  private JProgressBar             progressBar;
  private JLabel                   lblProgressAction;

  public MovieCleanUpUnwantedFilesDialog(List<Movie> selectedMovies) {
    super(BUNDLE.getString("movie.cleanupfiles"), "cleanupfiles");

    results = GlazedListsSwing.swingThreadProxyList(GlazedLists.threadSafeList(new BasicEventList<>()));
    DefaultEventTableModel<FileContainer> cleanUpTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(results),
        new CleanUpTableFormat());

    {
      table = new TmmTable(cleanUpTableModel);
      JScrollPane scrollPane = new JScrollPane(table);
      table.configureScrollPane(scrollPane);
      getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    {
      {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new MigLayout("", "[][grow]", "[]"));

        progressBar = new JProgressBar();
        infoPanel.add(progressBar, "cell 0 0");

        lblProgressAction = new JLabel("");
        infoPanel.add(lblProgressAction, "cell 1 0");

        setBottomInformationPanel(infoPanel);
      }
      {
        btnClean = new JButton(BUNDLE.getString("Button.deleteselected")); //$NON-NLS-1$
        btnClean.setIcon(IconManager.DELETE_INV);
        btnClean.addActionListener(arg0 -> cleanFiles(table));
        addButton(btnClean);

        JButton btnClose = new JButton(BUNDLE.getString("Button.close")); //$NON-NLS-1$
        btnClose.setIcon(IconManager.APPLY_INV);
        btnClose.addActionListener(arg0 -> setVisible(false));
        addButton(btnClose);
      }
    }

    MovieCleanUpWorker worker = new MovieCleanUpWorker(selectedMovies);
    worker.execute();
  }

  private class CleanUpTableFormat implements TableFormat<FileContainer> {
    @Override
    public int getColumnCount() {
      return 3;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.filename");

        case 1:
          return BUNDLE.getString("metatag.size");

        case 2:
          return BUNDLE.getString("metatag.filetype");
      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(FileContainer selectedFile, int column) {
      switch (column) {
        case 0:
          return selectedFile.file.toString();

        case 1:
          return selectedFile.getFilesizeInKilobytes();

        case 2:
          return selectedFile.getExtension();
      }
      throw new IllegalStateException();
    }
  }

  private class MovieCleanUpWorker extends SwingWorker<Void, Void> {

    private List<Movie>       selectedMovies;
    public Set<FileContainer> files;

    private MovieCleanUpWorker(List<Movie> selectedMovies) {
      this.selectedMovies = selectedMovies;
    }

    @Override
    protected Void doInBackground() {
      btnClean.setEnabled(false);
      startProgressBar();

      // Get Cleanup File Types from the settings
      List<String> filetypes = Settings.getInstance().getCleanupFileType();
      String extensions = String.join(",", filetypes);
      extensions = extensions.replace(".", "");

      for (Movie movie : selectedMovies) {
        for (File file : MovieHelpers.getUnknownFiles(movie.getPath(), "*.{" + extensions + "}")) {
          FileContainer fileContainer = new FileContainer();
          fileContainer.movie = movie;
          fileContainer.file = file.toPath();
          try {
            BasicFileAttributes attrs = Files.readAttributes(fileContainer.file, BasicFileAttributes.class);
            fileContainer.filesize = attrs.size();
          }
          catch (Exception ignored) {
          }

          results.add(fileContainer);
        }
      }

      return null;
    }

    @Override
    protected void done() {
      stopProgressBar();
      SwingUtilities.invokeLater(() -> {
        btnClean.setEnabled(true);
        TableColumnResizer.adjustColumnPreferredWidths(table);
        table.getParent().invalidate();
      });
    }
  }

  private void cleanFiles(JTable table) {
    // clean selected Files and remove them from the List
    int[] rows = table.getSelectedRows();
    List<FileContainer> fileList = new ArrayList<>();

    for (int row : rows) {
      try {
        FileContainer selectedFile = results.get(row);
        fileList.add(selectedFile);
        LOGGER.info("Deleting File " + selectedFile.file.toString());
        Utils.deleteFileWithBackup(selectedFile.file, selectedFile.movie.getDataSource());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }

    results.removeAll(fileList);
  }

  private void startProgressBar() {
    SwingUtilities.invokeLater(() -> {
      progressBar.setVisible(true);
      progressBar.setIndeterminate(true);
      lblProgressAction.setText("movie.searchunwanted");
    });
  }

  private void stopProgressBar() {
    SwingUtilities.invokeLater(() -> {
      progressBar.setVisible(false);
      progressBar.setIndeterminate(false);
      lblProgressAction.setText("");
    });
  }

  private class FileContainer {
    Movie movie;
    Path  file;
    long  filesize;

    String getFilesizeInKilobytes() {
      DecimalFormat df = new DecimalFormat("#0.00");
      return df.format(filesize / (1024.0)) + " kB";
    }

    String getExtension() {
      return FilenameUtils.getExtension(file.getFileName().toString());
    }
  }
}
