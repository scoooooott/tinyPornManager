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

package org.tinymediamanager.ui.dialogs;

import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
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
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.components.table.TmmTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import net.miginfocom.swing.MigLayout;

/**
 * show a dialog with all unwanted files
 *
 * @author Wolfgang Janes
 */
public class CleanUpUnwantedFilesDialog extends TmmDialog {

  private static final Logger                                 LOGGER = LoggerFactory.getLogger(CleanUpUnwantedFilesDialog.class);

  private EventList<CleanUpUnwantedFilesDialog.FileContainer> results;
  private TmmTable                                            table;
  private JButton                                             btnClean;
  private JProgressBar                                        progressBar;
  private JLabel                                              lblProgressAction;

  public CleanUpUnwantedFilesDialog(List<MediaEntity> selectedEntities) {
    super(BUNDLE.getString("cleanupfiles"), "cleanupEntities");

    results = GlazedListsSwing.swingThreadProxyList(GlazedLists.threadSafeList(new BasicEventList<>()));
    DefaultEventTableModel<CleanUpUnwantedFilesDialog.FileContainer> cleanUpTableModel = new DefaultEventTableModel<>(
        GlazedListsSwing.swingThreadProxyList(results), new CleanUpTableFormat());

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
        btnClean = new JButton(BUNDLE.getString("Button.deleteselected"));
        btnClean.setIcon(IconManager.DELETE_INV);
        btnClean.addActionListener(arg0 -> cleanFiles(table));
        addButton(btnClean);

        JButton btnClose = new JButton(BUNDLE.getString("Button.close"));
        btnClose.setIcon(IconManager.APPLY_INV);
        btnClose.addActionListener(arg0 -> setVisible(false));
        addButton(btnClose);
      }
    }

    TvShowCleanUpWorker worker = new TvShowCleanUpWorker(selectedEntities);
    worker.execute();

  }

  private static class CleanUpTableFormat implements TableFormat<CleanUpUnwantedFilesDialog.FileContainer> {
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
    public Object getColumnValue(CleanUpUnwantedFilesDialog.FileContainer selectedFile, int column) {
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

  private static class FileContainer {
    MediaEntity entity;
    Path        file;
    long        filesize;

    String getFilesizeInKilobytes() {
      DecimalFormat df = new DecimalFormat("#0.00");
      return df.format(filesize / (1000.0)) + " kB";
    }

    String getExtension() {
      return FilenameUtils.getExtension(file.getFileName().toString());
    }

    String getFileName() {
      return file.toString();
    }
  }

  private class TvShowCleanUpWorker extends SwingWorker<Void, Void> {

    private List<MediaEntity>                            selectedEntities;
    public Set<CleanUpUnwantedFilesDialog.FileContainer> files;

    private TvShowCleanUpWorker(List<MediaEntity> entities) {
      this.selectedEntities = entities;
    }

    @Override
    protected Void doInBackground() {
      btnClean.setEnabled(false);
      startProgressBar();

      // Get Cleanup File Types from the settings
      List<String> regexPatterns = Settings.getInstance().getCleanupFileType();

      selectedEntities.sort(Comparator.comparing(MediaEntity::getTitle));

      HashSet<Path> fileList = new HashSet<>();

      for (MediaEntity entity : selectedEntities) {
        for (Path file : Utils.getUnknownFilesByRegex(entity.getPathNIO(), regexPatterns)) {
          if (fileList.contains(file)) {
            continue;
          }

          CleanUpUnwantedFilesDialog.FileContainer fileContainer = new FileContainer();
          fileContainer.entity = entity;
          fileContainer.file = file;
          try {
            BasicFileAttributes attrs = Files.readAttributes(fileContainer.file, BasicFileAttributes.class);
            fileContainer.filesize = attrs.size();
          }
          catch (Exception ignored) {
          }

          results.add(fileContainer);
          fileList.add(file);
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
        results.sort(Comparator.comparing(FileContainer::getFileName));
      });
    }
  }

  private void cleanFiles(JTable table) {
    // clean selected Files and remove them from the List
    int[] rows = table.getSelectedRows();
    List<CleanUpUnwantedFilesDialog.FileContainer> fileList = new ArrayList<>();

    for (int row : rows) {
      try {
        CleanUpUnwantedFilesDialog.FileContainer selectedFile = results.get(row);
        fileList.add(selectedFile);
        LOGGER.info("Deleting File " + selectedFile.file.toString());
        Utils.deleteFileWithBackup(selectedFile.file, selectedFile.entity.getDataSource());
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
}
