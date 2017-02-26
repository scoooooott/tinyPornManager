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
package org.tinymediamanager.ui.panels;

import static org.tinymediamanager.core.MediaFileType.NFO;
import static org.tinymediamanager.core.MediaFileType.SAMPLE;
import static org.tinymediamanager.core.MediaFileType.TRAILER;
import static org.tinymediamanager.core.MediaFileType.VIDEO;
import static org.tinymediamanager.core.MediaFileType.VIDEO_EXTRA;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.UTF8Control;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The class MediaFileEditorPanel is used to maintain associated media files
 *
 * @author Manuel Laggner
 */
public class MediaFileEditorPanel extends JPanel {
  private static final long                                                   serialVersionUID = -2416409052145301941L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle                                         BUNDLE           = ResourceBundle.getBundle("messages",              //$NON-NLS-1$
      new UTF8Control());

  private List<MediaFileContainer>                                            mediaFiles;
  private JTable                                                              tableMediaFiles;
  private JLabel                                                              lblFilename;
  private JTextField                                                          tfCodec;
  private JTextField                                                          tfContainerFormat;
  private JTextField                                                          tfWidth;
  private JTextField                                                          tfHeight;
  private JTable                                                              tableAudioStreams;
  private JTable                                                              tableSubtitles;
  private JButton                                                             btnAddAudioStream;
  private JButton                                                             btnRemoveAudioStream;
  private JButton                                                             btnAddSubtitle;
  private JButton                                                             btnRemoveSubtitle;
  private JComboBox<String>                                                   cb3dFormat;
  private JTableBinding<MediaFileSubtitle, JTable, JTable>                    jTableBinding_2;
  private JTableBinding<MediaFileAudioStream, JTable, JTable>                 jTableBinding_1;
  private JTableBinding<MediaFileContainer, List<MediaFileContainer>, JTable> jTableBinding;

  public MediaFileEditorPanel(List<MediaFile> mediaFiles) {
    this.mediaFiles = ObservableCollections.observableList(new ArrayList<MediaFileContainer>());
    for (MediaFile mediaFile : mediaFiles) {
      MediaFileContainer container = new MediaFileContainer(mediaFile);
      this.mediaFiles.add(container);
    }

    setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));
    {
      JSplitPane splitPane = new JSplitPane();
      add(splitPane, "2, 2, fill, fill");
      {
        JPanel panelMediaFiles = new JPanel();
        panelMediaFiles.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("175dlu:grow"), },
            new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
        JScrollPane scrollPaneMediaFiles = new JScrollPane();
        panelMediaFiles.add(scrollPaneMediaFiles, "1, 2, fill, fill");
        splitPane.setLeftComponent(panelMediaFiles);
        {
          tableMediaFiles = new JTable();
          scrollPaneMediaFiles.setViewportView(tableMediaFiles);
        }
      }
      {
        JPanel panelDetails = new JPanel();
        splitPane.setRightComponent(panelDetails);
        panelDetails.setLayout(new FormLayout(
            new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                ColumnSpec.decode("15dlu"), FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC, },
            new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("15dlu"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, }));
        {
          lblFilename = new JLabel("");
          panelDetails.add(lblFilename, "2, 2, 7, 1");
        }
        {
          JLabel lblCodec = new JLabel("Codec");
          panelDetails.add(lblCodec, "2, 4, right, default");
        }
        {
          tfCodec = new JTextField();
          panelDetails.add(tfCodec, "4, 4, fill, default");
          tfCodec.setColumns(10);
        }
        {
          JLabel lblContainerFormat = new JLabel("Container format");
          panelDetails.add(lblContainerFormat, "6, 4, right, default");
        }
        {
          tfContainerFormat = new JTextField();
          panelDetails.add(tfContainerFormat, "8, 4, fill, default");
          tfContainerFormat.setColumns(10);
        }
        {
          JLabel lblWidth = new JLabel("Width");
          panelDetails.add(lblWidth, "2, 6, right, default");
        }
        {
          tfWidth = new JTextField();
          panelDetails.add(tfWidth, "4, 6, fill, default");
          tfWidth.setColumns(10);
        }
        {
          JLabel lblHeight = new JLabel("Height");
          panelDetails.add(lblHeight, "6, 6, right, default");
        }
        {
          tfHeight = new JTextField();
          panelDetails.add(tfHeight, "8, 6, fill, default");
          tfHeight.setColumns(10);
        }
        {
          JLabel lbld = new JLabel("3D Format");
          panelDetails.add(lbld, "2, 8, right, default");
        }
        {
          cb3dFormat = new JComboBox<>();
          cb3dFormat.addItem("");
          cb3dFormat.addItem(MediaFile.VIDEO_3D);
          cb3dFormat.addItem(MediaFile.VIDEO_3D_SBS);
          cb3dFormat.addItem(MediaFile.VIDEO_3D_HSBS);
          cb3dFormat.addItem(MediaFile.VIDEO_3D_TAB);
          cb3dFormat.addItem(MediaFile.VIDEO_3D_HTAB);
          panelDetails.add(cb3dFormat, "4, 8, fill, default");
        }
        {
          JLabel lblAudiostreams = new JLabel("AudioStreams");
          panelDetails.add(lblAudiostreams, "2, 10, right, default");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelDetails.add(scrollPane, "4, 10, 5, 5, fill, fill");
          {
            tableAudioStreams = new JTable();
            scrollPane.setViewportView(tableAudioStreams);
          }
        }
        {
          btnAddAudioStream = new JButton("");
          btnAddAudioStream.setAction(new AddAudioStreamAction());
          btnAddAudioStream.setMargin(new Insets(2, 2, 2, 2));
          btnAddAudioStream.setIcon(IconManager.LIST_ADD);
          panelDetails.add(btnAddAudioStream, "2, 12, right, top");
        }
        {
          btnRemoveAudioStream = new JButton("");
          btnRemoveAudioStream.setAction(new RemoveAudioStreamAction());
          btnRemoveAudioStream.setMargin(new Insets(2, 2, 2, 2));
          btnRemoveAudioStream.setIcon(IconManager.LIST_REMOVE);
          panelDetails.add(btnRemoveAudioStream, "2, 14, right, top");
        }
        {
          JLabel lblSubtitles = new JLabel("Subtitles");
          panelDetails.add(lblSubtitles, "2, 16, right, default");
        }
        {
          JScrollPane scrollPane = new JScrollPane();
          panelDetails.add(scrollPane, "4, 16, 5, 5, fill, fill");
          {
            tableSubtitles = new JTable();
            scrollPane.setViewportView(tableSubtitles);
          }
        }
        {
          btnAddSubtitle = new JButton("");
          btnAddSubtitle.setAction(new AddSubtitleAction());
          btnAddSubtitle.setMargin(new Insets(2, 2, 2, 2));
          btnAddSubtitle.setIcon(IconManager.LIST_ADD);
          panelDetails.add(btnAddSubtitle, "2, 18, right, top");
        }
        {
          btnRemoveSubtitle = new JButton("");
          btnRemoveSubtitle.setAction(new RemoveSubtitleAction());
          btnRemoveSubtitle.setMargin(new Insets(2, 2, 2, 2));
          btnRemoveSubtitle.setIcon(IconManager.LIST_REMOVE);
          panelDetails.add(btnRemoveSubtitle, "2, 20, right, top");
        }
      }
    }

    initDataBindings();

    // select first
    if (!this.mediaFiles.isEmpty()) {
      tableMediaFiles.getSelectionModel().setSelectionInterval(0, 0);
    }

    // add selection listener to disable editing when needed
    tableMediaFiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      private Set<MediaFileType> videoTypes = new HashSet<>(Arrays.asList(VIDEO, VIDEO_EXTRA, SAMPLE, TRAILER));

      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        if (!arg0.getValueIsAdjusting()) {
          int selectedRow = tableMediaFiles.convertRowIndexToModel(tableMediaFiles.getSelectedRow());
          if (selectedRow > -1) {
            MediaFile mf = MediaFileEditorPanel.this.mediaFiles.get(selectedRow).getMediaFile();
            // codec should not be enabled for NFOs
            tfCodec.setEnabled(!(mf.getType() == NFO));
            // audio streams and subtitles should not be enabled for anything except VIDEOS/TRAILER/SAMPLES
            btnAddAudioStream.setEnabled(videoTypes.contains(mf.getType()));
            btnRemoveAudioStream.setEnabled(videoTypes.contains(mf.getType()));
            btnAddSubtitle.setEnabled(videoTypes.contains(mf.getType()));
            btnRemoveSubtitle.setEnabled(videoTypes.contains(mf.getType()));
            // 3D is only available for video types
            cb3dFormat.setEnabled(videoTypes.contains(mf.getType()));
          }
        }
      }
    });
  }

  private class AddAudioStreamAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414523349267L;

    public AddAudioStreamAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("audiostream.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int mediaFileRow = tableMediaFiles.getSelectedRow();
      if (mediaFileRow > -1) {
        mediaFileRow = tableMediaFiles.convertRowIndexToModel(mediaFileRow);
        MediaFileContainer mf = mediaFiles.get(mediaFileRow);
        mf.addAudioStream(new MediaFileAudioStream());
      }
    }
  }

  private class RemoveAudioStreamAction extends AbstractAction {
    private static final long serialVersionUID = -7079826940827356996L;

    public RemoveAudioStreamAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("audiostream.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableAudioStreams.getSelectedRow();
      if (row > -1) {
        row = tableAudioStreams.convertRowIndexToModel(row);
        int mediaFileRow = tableMediaFiles.getSelectedRow();
        if (mediaFileRow > -1) {
          mediaFileRow = tableMediaFiles.convertRowIndexToModel(mediaFileRow);
          MediaFileContainer mf = mediaFiles.get(mediaFileRow);
          MediaFileAudioStream audioStream = mf.getAudioStreams().get(row);
          mf.removeAudioStream(audioStream);
        }
      }
    }
  }

  private class AddSubtitleAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414523349767L;

    public AddSubtitleAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("subtitle.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int mediaFileRow = tableMediaFiles.getSelectedRow();
      if (mediaFileRow > -1) {
        mediaFileRow = tableMediaFiles.convertRowIndexToModel(mediaFileRow);
        MediaFileContainer mf = mediaFiles.get(mediaFileRow);
        mf.addSubtitle(new MediaFileSubtitle());
      }
    }
  }

  private class RemoveSubtitleAction extends AbstractAction {
    private static final long serialVersionUID = -7079866940827356996L;

    public RemoveSubtitleAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("subtitle.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableSubtitles.getSelectedRow();
      if (row > -1) {
        row = tableSubtitles.convertRowIndexToModel(row);
        int mediaFileRow = tableMediaFiles.getSelectedRow();
        if (mediaFileRow > -1) {
          mediaFileRow = tableMediaFiles.convertRowIndexToModel(mediaFileRow);
          MediaFileContainer mf = mediaFiles.get(mediaFileRow);
          MediaFileSubtitle subtitle = mf.getSubtitles().get(row);
          mf.removeSubtitle(subtitle);
        }
      }
    }
  }

  /*
   * Container needed to make the audio streams and subtitles editable
   */
  public class MediaFileContainer {
    private MediaFile                  mediaFile;
    private List<MediaFileAudioStream> audioStreams;
    private List<MediaFileSubtitle>    subtitles;

    private MediaFileContainer(MediaFile mediaFile) {
      this.mediaFile = mediaFile;
      this.audioStreams = ObservableCollections.observableList(mediaFile.getAudioStreams());
      this.subtitles = ObservableCollections.observableList(mediaFile.getSubtitles());
    }

    public MediaFile getMediaFile() {
      return mediaFile;
    }

    public List<MediaFileAudioStream> getAudioStreams() {
      return audioStreams;
    }

    public void addAudioStream(MediaFileAudioStream audioStream) {
      if (!audioStreams.contains(audioStream)) {
        audioStreams.add(audioStream);
      }
    }

    public void removeAudioStream(MediaFileAudioStream audioStream) {
      audioStreams.remove(audioStream);
    }

    public List<MediaFileSubtitle> getSubtitles() {
      return subtitles;
    }

    public void addSubtitle(MediaFileSubtitle subtitle) {
      if (!subtitles.contains(subtitle)) {
        subtitles.add(subtitle);
      }
    }

    public void removeSubtitle(MediaFileSubtitle subtitle) {
      subtitles.remove(subtitle);
    }

  }

  /**
   * Sync media files edited from this editor with the ones from the media entity without removing/adding all of them
   * 
   * @param mfsFromEditor
   *          the edited media files
   * @param mfsFromMediaEntity
   *          the original media files
   */
  public static void syncMediaFiles(List<MediaFile> mfsFromEditor, List<MediaFile> mfsFromMediaEntity) {
    for (MediaFile mfEditor : mfsFromEditor) {
      for (MediaFile mfOriginal : mfsFromMediaEntity) {
        if (mfEditor.equals(mfOriginal)) {
          // here we check all field which can be edited from the editor
          if (!mfEditor.getVideoCodec().equals(mfOriginal.getVideoCodec())) {
            mfOriginal.setVideoCodec(mfEditor.getVideoCodec());
          }
          if (!mfEditor.getContainerFormat().equals(mfOriginal.getContainerFormat())) {
            mfOriginal.setContainerFormat(mfEditor.getContainerFormat());
          }
          if (mfEditor.getVideoWidth() != mfOriginal.getVideoWidth()) {
            mfOriginal.setVideoWidth(mfEditor.getVideoWidth());
          }
          if (mfEditor.getVideoHeight() != mfOriginal.getVideoHeight()) {
            mfOriginal.setVideoHeight(mfEditor.getVideoHeight());
          }
          if (!mfEditor.getVideo3DFormat().equals(mfOriginal.getVideo3DFormat())) {
            mfOriginal.setVideo3DFormat(mfEditor.getVideo3DFormat());
          }
          // audio streams and subtitles will be completely set
          mfOriginal.setAudioStreams(mfEditor.getAudioStreams());
          mfOriginal.setSubtitles(mfEditor.getSubtitles());
          break;
        }
      }
    }
  }

  protected void initDataBindings() {
    jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, mediaFiles, tableMediaFiles);
    //
    BeanProperty<MediaFileContainer, String> mediaFileContainerBeanProperty = BeanProperty.create("mediaFile.filename");
    jTableBinding.addColumnBinding(mediaFileContainerBeanProperty).setColumnName("Filename").setEditable(false);
    //
    jTableBinding.setEditable(false);
    jTableBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.mediaFile.filename");
    BeanProperty<JLabel, String> jLabelBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JLabel, String> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty, lblFilename, jLabelBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_1 = BeanProperty.create("selectedElement.mediaFile.videoCodec");
    BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty_1, tfCodec, jTextFieldBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_3 = BeanProperty.create("selectedElement.mediaFile.containerFormat");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
    AutoBinding<JTable, String, JTextField, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty_3, tfContainerFormat, jTextFieldBeanProperty_2);
    autoBinding_3.bind();
    //
    BeanProperty<JTable, Integer> jTableBeanProperty_5 = BeanProperty.create("selectedElement.mediaFile.videoWidth");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
    AutoBinding<JTable, Integer, JTextField, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty_5, tfWidth, jTextFieldBeanProperty_4);
    autoBinding_5.bind();
    //
    BeanProperty<JTable, Integer> jTableBeanProperty_6 = BeanProperty.create("selectedElement.mediaFile.videoHeight");
    BeanProperty<JTextField, String> jTextFieldBeanProperty_5 = BeanProperty.create("text");
    AutoBinding<JTable, Integer, JTextField, String> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty_6, tfHeight, jTextFieldBeanProperty_5);
    autoBinding_6.bind();
    //
    BeanProperty<JTable, List<MediaFileAudioStream>> jTableBeanProperty_2 = BeanProperty.create("selectedElement.audioStreams");
    jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, tableMediaFiles, jTableBeanProperty_2, tableAudioStreams);
    //
    BeanProperty<MediaFileAudioStream, String> mediaFileAudioStreamBeanProperty = BeanProperty.create("language");
    jTableBinding_1.addColumnBinding(mediaFileAudioStreamBeanProperty).setColumnName("Language").setColumnClass(String.class);
    //
    BeanProperty<MediaFileAudioStream, String> mediaFileAudioStreamBeanProperty_1 = BeanProperty.create("codec");
    jTableBinding_1.addColumnBinding(mediaFileAudioStreamBeanProperty_1).setColumnName("Codec");
    //
    BeanProperty<MediaFileAudioStream, String> mediaFileAudioStreamBeanProperty_2 = BeanProperty.create("channels");
    jTableBinding_1.addColumnBinding(mediaFileAudioStreamBeanProperty_2).setColumnName("Channels");
    //
    BeanProperty<MediaFileAudioStream, Integer> mediaFileAudioStreamBeanProperty_3 = BeanProperty.create("bitrate");
    jTableBinding_1.addColumnBinding(mediaFileAudioStreamBeanProperty_3).setColumnName("Bitrate").setColumnClass(Integer.class);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<JTable, List<MediaFileSubtitle>> jTableBeanProperty_4 = BeanProperty.create("selectedElement.subtitles");
    jTableBinding_2 = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, tableMediaFiles, jTableBeanProperty_4, tableSubtitles);
    //
    BeanProperty<MediaFileSubtitle, String> mediaFileSubtitleBeanProperty = BeanProperty.create("language");
    jTableBinding_2.addColumnBinding(mediaFileSubtitleBeanProperty).setColumnName("Language").setColumnClass(String.class);
    //
    BeanProperty<MediaFileSubtitle, Boolean> mediaFileSubtitleBeanProperty_1 = BeanProperty.create("forced");
    jTableBinding_2.addColumnBinding(mediaFileSubtitleBeanProperty_1).setColumnName("Forced").setColumnClass(Boolean.class);
    //
    jTableBinding_2.bind();
    //
    BeanProperty<JTable, String> jTableBeanProperty_7 = BeanProperty.create("selectedElement.mediaFile.video3DFormat");
    BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<JTable, String, JComboBox, Object> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tableMediaFiles,
        jTableBeanProperty_7, cb3dFormat, jComboBoxBeanProperty);
    autoBinding_2.bind();
  }

  public void unbindBindings() {
    jTableBinding.unbind();
    jTableBinding_1.unbind();
    jTableBinding_2.unbind();
  }
}
