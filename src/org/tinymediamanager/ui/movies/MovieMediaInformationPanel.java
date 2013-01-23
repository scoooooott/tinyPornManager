/*
 * Copyright 2012 Manuel Laggner
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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.ui.LinkLabel;
import org.tinymediamanager.ui.TableColumnAdjuster;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MovieMediaInformationPanel.
 */
public class MovieMediaInformationPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long          serialVersionUID    = 1L;

  /** The logger. */
  private final static Logger        LOGGER              = Logger.getLogger(MovieMediaInformationPanel.class);

  /** The movie selection model. */
  private MovieSelectionModel        movieSelectionModel;

  /** The lbl files t. */
  private JLabel                     lblFilesT;

  /** The scroll pane files. */
  private JScrollPane                scrollPaneFiles;

  /** The table files. */
  private JTable                     tableFiles;

  /** The lbl movie path. */
  private LinkLabel                  lblMoviePath;

  /** The lbl date added t. */
  private JLabel                     lblDateAddedT;

  /** The lbl date added. */
  private JLabel                     lblDateAdded;

  /** The cb watched. */
  private JCheckBox                  cbWatched;

  /** The lbl watched t. */
  private JLabel                     lblWatchedT;

  /** The lbl movie path t. */
  private JLabel                     lblMoviePathT;

  /** The btn play. */
  private JButton                    btnPlay;

  /** The table column adjuster. */
  private TableColumnAdjuster        tableColumnAdjuster = null;

  /** The media file event list. */
  private EventList<MediaFile>       mediaFileEventList;

  /** The media file table model. */
  private EventTableModel<MediaFile> mediaFileTableModel = null;

  /**
   * Instantiates a new movie media information panel.
   * 
   * @param model
   *          the model
   */
  public MovieMediaInformationPanel(MovieSelectionModel model) {
    this.movieSelectionModel = model;
    mediaFileEventList = new ObservableElementList<MediaFile>(GlazedLists.threadSafeList(new BasicEventList<MediaFile>()),
        GlazedLists.beanConnector(MediaFile.class));

    setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
        ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC,
            FormFactory.DEFAULT_ROWSPEC, FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

    lblDateAddedT = new JLabel("Added on");
    add(lblDateAddedT, "2, 2");

    lblDateAdded = new JLabel("");
    add(lblDateAdded, "4, 2");

    lblWatchedT = new JLabel("Watched");
    add(lblWatchedT, "6, 2");

    cbWatched = new JCheckBox("");
    cbWatched.setEnabled(false);
    add(cbWatched, "8, 2");

    // btnPlay = new JButton("Play");
    // btnPlay.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent arg0) {
    // if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
    // // get the location from the label
    // StringBuilder movieFile = new
    // StringBuilder(lblMoviePath.getNormalText());
    // movieFile.append(File.separator);
    // movieFile.append(movieSelectionModel.getSelectedMovie().getMediaFiles().get(0).getFilename());
    // File f = new File(movieFile.toString());
    //
    // try {
    // if (f.exists()) {
    //
    // String vlcF = f.getAbsolutePath();
    // // FIXME: german umlauts do not decode correctly; Bug in
    // // libDvdNav? so workaround;
    // if (vlcF.matches(".*[äöüÄÖÜ].*")) {
    // LOGGER.debug("VLC: workaround: german umlauts found - use system player");
    // Desktop.getDesktop().open(f);
    // }
    // else {
    // try {
    //
    // if (!vlcF.startsWith("/")) {
    // // add the missing 3rd / if not start with one (eg windows)
    // vlcF = "/" + vlcF;
    // }
    // String mrl = new FileMrl().file(vlcF).value();
    //
    // final EmbeddedMediaPlayerComponent mediaPlayerComponent = new
    // EmbeddedMediaPlayerComponent();
    // JFrame frame = new JFrame("player");
    // frame.setLocation(100, 100);
    // frame.setSize(1050, 600);
    // frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    // frame.setVisible(true);
    // frame.setContentPane(mediaPlayerComponent);
    // // mrl = mrl.replace("file://", ""); // does not work either
    //
    // LOGGER.debug("VLC: playing " + mrl);
    // Boolean ok = mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
    // if (!ok) {
    // LOGGER.error("VLC: couldn't create player window!");
    // }
    // }
    // catch (RuntimeException e) {
    // LOGGER.warn("VLC: has not been initialized on startup - use system player");
    // Desktop.getDesktop().open(f);
    // }
    // catch (NoClassDefFoundError e) {
    // LOGGER.warn("VLC: has not been initialized on startup - use system player");
    // Desktop.getDesktop().open(f);
    // }
    //
    // } // end else
    // } // end exists
    // } // end try
    // catch (IOException e) {
    // LOGGER.error("Error opening file", e);
    // }
    // } // end isEmpty
    // }
    // });
    // add(btnPlay, "10, 2");

    lblMoviePathT = new JLabel("Path");
    add(lblMoviePathT, "2, 4");

    lblMoviePath = new LinkLabel("");
    lblMoviePath.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (!StringUtils.isEmpty(lblMoviePath.getNormalText())) {
          try {
            // get the location from the label
            File path = new File(lblMoviePath.getNormalText());
            // check whether this location exists
            if (path.exists()) {
              Desktop.getDesktop().open(path);
            }
          }
          catch (Exception ex) {
            LOGGER.error("open filemanager", ex);
          }
        }
      }
    });
    lblMoviePathT.setLabelFor(lblMoviePath);
    lblMoviePathT.setLabelFor(lblMoviePath);
    add(lblMoviePath, "4, 4, 5, 1");

    lblFilesT = new JLabel("Files");
    add(lblFilesT, "2, 6, default, top");

    scrollPaneFiles = new JScrollPane();
    add(scrollPaneFiles, "4, 6, 5, 1, fill, fill");

    mediaFileTableModel = new EventTableModel<MediaFile>(mediaFileEventList, new MediaTableFormat());
    tableFiles = new JTable(mediaFileTableModel);
    tableFiles.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    lblFilesT.setLabelFor(tableFiles);
    scrollPaneFiles.setViewportView(tableFiles);

    initDataBindings();

    // adjust table
    tableColumnAdjuster = new TableColumnAdjuster(tableFiles);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.setOnlyAdjustLarger(false);
    // tableColumnAdjuster.setDynamicAdjustment(true);

    // install the propertychangelistener
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String property = propertyChangeEvent.getPropertyName();
        Object source = propertyChangeEvent.getSource();
        // react on selection of a movie and change of media files
        if ((source.getClass() == MovieSelectionModel.class && "selectedMovie".equals(property))
            || (source.getClass() == Movie.class && "mediaFiles".equals(property))) {
          mediaFileEventList.clear();
          mediaFileEventList.addAll(movieSelectionModel.getSelectedMovie().getMediaFiles());
          tableColumnAdjuster.adjustColumns();
        }

      }
    };

    movieSelectionModel.addPropertyChangeListener(propertyChangeListener);
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty = BeanProperty.create("selectedMovie.dateAdded.date");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty, lblDateAdded, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSelectionModel, Boolean> movieSelectionModelBeanProperty_1 = BeanProperty.create("selectedMovie.watched");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSelectionModel, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
        movieSelectionModel, movieSelectionModelBeanProperty_1, cbWatched, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSelectionModel, Integer> movieSelectionModelBeanProperty_2 = BeanProperty.create("selectedMovie.dateAdded.day");
    AutoBinding<MovieSelectionModel, Integer, JLabel, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_2, lblDateAdded, jLabelBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_3 = BeanProperty.create("selectedMovie.dateAddedAsString");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_3, lblDateAdded, jLabelBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSelectionModel, String> movieSelectionModelBeanProperty_13 = BeanProperty.create("selectedMovie.path");
    AutoBinding<MovieSelectionModel, String, JLabel, String> autoBinding_19 = Bindings.createAutoBinding(UpdateStrategy.READ, movieSelectionModel,
        movieSelectionModelBeanProperty_13, lblMoviePath, jLabelBeanProperty);
    autoBinding_19.bind();
    // //
    // BeanProperty<MovieSelectionModel, List<MediaFile>>
    // movieSelectionModelBeanProperty_18 =
    // BeanProperty.create("selectedMovie.mediaFiles");
    // JTableBinding<MediaFile, MovieSelectionModel, JTable> jTableBinding =
    // SwingBindings.createJTableBinding(UpdateStrategy.READ,
    // movieSelectionModel,
    // movieSelectionModelBeanProperty_18, tableFiles);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty =
    // BeanProperty.create("filename");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty).setColumnName("Filename").setEditable(false);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty_1 =
    // BeanProperty.create("filesizeInMegabytes");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty_1).setColumnName("Size").setEditable(false);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty_2 =
    // BeanProperty.create("videoCodec");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty_2).setColumnName("Video codec").setEditable(false);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty_3 =
    // BeanProperty.create("videoResolution");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty_3).setColumnName("Resolution").setEditable(false);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty_4 =
    // BeanProperty.create("audioCodec");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty_4).setColumnName("Audio Codec").setEditable(false);
    // //
    // BeanProperty<MediaFile, String> mediaFileBeanProperty_5 =
    // BeanProperty.create("audioChannels");
    // jTableBinding.addColumnBinding(mediaFileBeanProperty_5).setColumnName("Audio channels").setEditable(false);
    // //
    // jTableBinding.setEditable(false);
    // jTableBinding.bind();
  }

  /**
   * The Class MediaTableFormat.
   */
  private static class MediaTableFormat implements AdvancedTableFormat<MediaFile> {

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    @Override
    public int getColumnCount() {
      return 8;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "Filename";

        case 1:
          return "Size";

        case 2:
          return "Runtime";

        case 3:
          return "VCodec";

        case 4:
          return "Resolution";

        case 5:
          return "VBitrate";

        case 6:
          return "ACodec";

        case 7:
          return "AChannels";

      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object,
     * int)
     */
    @Override
    public Object getColumnValue(MediaFile mediaFile, int column) {
      switch (column) {
        case 0:
          return mediaFile.getFilename();

        case 1:
          return mediaFile.getFilesizeInMegabytes();

        case 2:
          return mediaFile.getDurationHM();

        case 3:
          return mediaFile.getVideoCodec();

        case 4:
          return mediaFile.getVideoResolution();

        case 5:
          return mediaFile.getBiteRateInKbps();

        case 6:
          return mediaFile.getAudioCodec();

        case 7:
          return mediaFile.getAudioChannels();
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    @Override
    public Class getColumnClass(int column) {
      switch (column) {
        case 0:
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
          return String.class;
      }

      throw new IllegalStateException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    @Override
    public Comparator getColumnComparator(int arg0) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
