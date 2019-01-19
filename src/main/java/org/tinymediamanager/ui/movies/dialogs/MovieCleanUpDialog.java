package org.tinymediamanager.ui.movies.dialogs;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieHelpers;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.components.table.TmmTable;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MovieCleanUpDialog extends TmmDialog {

  private TmmTable table;
  private JButton clean;
  private JPanel bottomPanel;
  private EventList<File> results;
  private JProgressBar progressBar;
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieCleanUpDialog.class);


  public MovieCleanUpDialog(List<Movie> selectedMovies) {
    super(BUNDLE.getString("movie.cleanupfiles"),"cleanupfiles");


    results = GlazedListsSwing.swingThreadProxyList(GlazedLists.threadSafeList(new BasicEventList<>()));
    DefaultEventTableModel<File> cleanUpTableModel = new DefaultEventTableModel<>(GlazedListsSwing.swingThreadProxyList(results)
            , new CleanUpTableFormat());

    clean = new JButton(BUNDLE.getString("movie.cleanupfiles"));
    progressBar = new JProgressBar();
    bottomPanel = new JPanel();


    clean.addActionListener(arg0-> cleanFiles(table));
    table = new TmmTable(cleanUpTableModel);
    bottomPanel.setLayout(new GridLayout());
    bottomPanel.add(progressBar);
    bottomPanel.add(clean);

    this.setLayout(new BorderLayout());
    this.add(new JScrollPane(table), BorderLayout.CENTER);
    this.add(bottomPanel,BorderLayout.SOUTH);
    this.pack();

    MovieCleanUpWorker worker = new MovieCleanUpWorker(selectedMovies);
    worker.execute();

  }

  private class CleanUpTableFormat implements TableFormat<File> {

    @Override
    public int getColumnCount() {
      return 1;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.name");
      }
      throw new IllegalStateException();
    }

    @Override
    public Object getColumnValue(File selectedFile, int column) {
      switch (column) {
        case 0:
          return selectedFile.getAbsolutePath();
      }
      throw new IllegalStateException();
    }
  }

  private class MovieCleanUpWorker extends SwingWorker<Void,Void> {

    private List<Movie> selectedMovies;
    public Set<File> files;

    private MovieCleanUpWorker(List<Movie> selectedMovies) {
      this.selectedMovies = selectedMovies;
    }

    @Override
    protected Void doInBackground() {
      clean.setEnabled(false);
      startProgressBar();

      //Get Cleanup File Types from the settings
      List<String> filetypes = Settings.getInstance().getCleanupFileType();
      String extensions = String.join(",", filetypes);
      extensions = extensions.replace(".", "");

      for (Movie movie : selectedMovies) {
        results.addAll(MovieHelpers.getUnknownFiles(movie.getPath(),"*.{" + extensions + "}"));
      }

      return null;
    }

    @Override
    protected void done() {
      stopProgressBar();
      clean.setEnabled(true);
    }


  }

  private void cleanFiles(JTable table) {

    // clean selected Files and remove them from the List

    int[] rows = table.getSelectedRows();
    List<File> fileList = new ArrayList<>();

    for (int row : rows) {

      try {
        fileList.add(results.get(row));
        LOGGER.info("Delete File " + results.get(row).getName());
        results.get(row).delete();

      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    results.removeAll(fileList);

    if (table.getRowCount() == 0) {
      this.setVisible(false);
    }

  }

  private void startProgressBar() {
    SwingUtilities.invokeLater(() -> {
      progressBar.setVisible(true);
      progressBar.setIndeterminate(true);
    });
  }

  private void stopProgressBar() {
    SwingUtilities.invokeLater(() -> {
      progressBar.setVisible(false);
      progressBar.setIndeterminate(false);
    });
  }

}
