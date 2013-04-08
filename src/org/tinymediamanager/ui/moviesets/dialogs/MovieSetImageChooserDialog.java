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
package org.tinymediamanager.ui.moviesets.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.ImageSizeAndUrl;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.ToggleButtonUI;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.WrapLayout;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSetImageChooser.
 * 
 * @author Manuel Laggner
 */
public class MovieSetImageChooserDialog extends JDialog {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());  //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger         LOGGER           = LoggerFactory.getLogger(MovieSetImageChooserDialog.class);

  /**
   * The Enum ImageType.
   * 
   * @author Manuel Laggner
   */
  public enum ImageType {

    /** The poster. */
    POSTER,
    /** The fanart. */
    FANART
  }

  /** The content panel. */
  private final JPanel         contentPanel   = new JPanel();

  /** The progress bar. */
  private JProgressBar         progressBar;

  /** The lbl progress action. */
  private JLabel               lblProgressAction;

  /** The panel images. */
  private JPanel               panelImages;

  /** The image label. */
  private ImageLabel           imageLabel;

  /** The type. */
  private ImageType            type;

  /** The button group. */
  private ButtonGroup          buttonGroup    = new ButtonGroup();

  /** The buttons. */
  private List<JToggleButton>  buttons        = new ArrayList<JToggleButton>();

  /** The task. */
  private DownloadTask         task;

  /** The action ok. */
  private final Action         actionOK       = new SwingAction();

  /** The action cancel. */
  private final Action         actionCancel   = new SwingAction_1();

  /** The toggle button ui. */
  private final ToggleButtonUI toggleButtonUI = new ToggleButtonUI();

  /** The action. */
  private final Action         action         = new SwingAction_2();

  /** The movie list. */
  private final MovieList      movieList      = MovieList.getInstance();

  /**
   * Create the dialog.
   * 
   * @param tmdbId
   *          the tmdb id
   * @param poster
   *          the type
   * @param imageLabel
   *          the image label
   */
  public MovieSetImageChooserDialog(int tmdbId, ImageType poster, ImageLabel imageLabel) {
    setModal(true);
    setIconImage(Globals.logo);
    this.imageLabel = imageLabel;
    this.type = poster;

    switch (poster) {
      case FANART:
        setTitle(BUNDLE.getString("image.choose.fanart")); //$NON-NLS-1$
        break;

      case POSTER:
        setTitle(BUNDLE.getString("image.choose.poster")); //$NON-NLS-1$
        break;
    }

    setName("imageChooser");

    setBounds(5, 5, 1000, 590);
    TmmWindowSaver.loadSettings(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("258px:grow"), },
        new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:266px:grow"), }));
    {
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      contentPanel.add(scrollPane, "2, 2, fill, fill");
      {
        panelImages = new JPanel();
        scrollPane.setViewportView(panelImages);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panelImages.setLayout(new WrapLayout(FlowLayout.LEFT));
      }
    }
    {
      JPanel buttonPane = new JPanel();
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), }));
      {
        progressBar = new JProgressBar();
        buttonPane.add(progressBar, "2, 2");
      }
      {
        lblProgressAction = new JLabel("");
        buttonPane.add(lblProgressAction, "4, 2");
      }
      {
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "6, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton btnAddFile = new JButton(BUNDLE.getString("Button.addfile")); //$NON-NLS-1$
        btnAddFile.setAction(action);
        buttonPane.add(btnAddFile, "8, 2, fill, top");
      }
      {
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "10, 2, fill, top");
      }
    }

    task = new DownloadTask(tmdbId);
    task.execute();
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
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("image.seteselected")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaArtwork artwork = null;
      ImageSizeAndUrl resolution = null;

      // get selected button
      for (JToggleButton button : buttons) {
        if (button.isSelected()) {
          Object clientProperty = button.getClientProperty("MediaArtwork");
          if (clientProperty instanceof MediaArtwork) {
            artwork = (MediaArtwork) clientProperty;
            clientProperty = button.getClientProperty("MediaArtworkSize");
            // try to get the size
            if (clientProperty instanceof JComboBox) {
              JComboBox cb = (JComboBox) clientProperty;
              resolution = (ImageSizeAndUrl) cb.getSelectedItem();
            }
            break;
          }
        }
      }

      // nothing selected
      if (artwork == null) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("image.noneselected")); //$NON-NLS-1$
        return;
      }

      if (artwork != null) {
        if (resolution != null) {
          imageLabel.setImageUrl(resolution.getUrl());
        }
        else {
          imageLabel.setImageUrl(artwork.getDefaultUrl());
        }
      }

      task.cancel(true);
      setVisible(false);
      dispose();
    }
  }

  /**
   * Start progress bar.
   * 
   * @param description
   *          the description
   */
  private void startProgressBar(String description) {
    lblProgressAction.setText(description);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
  }

  /**
   * Stop progress bar.
   */
  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    progressBar.setIndeterminate(false);
  }

  /**
   * Adds the image.
   * 
   * @param originalImage
   *          the original image
   * @param artwork
   *          the tmdb artwork
   */
  private void addImage(BufferedImage originalImage, MediaArtwork artwork) {
    int imageType = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
    Point size = null;

    GridBagLayout gbl = new GridBagLayout();

    switch (type) {
      case FANART:
        gbl.columnWidths = new int[] { 300 };
        gbl.rowHeights = new int[] { 150 };
        size = ImageLabel.calculateSize(300, 150, originalImage.getWidth(), originalImage.getHeight(), true);
        break;

      case POSTER:
      default:
        gbl.columnWidths = new int[] { 150 };
        gbl.rowHeights = new int[] { 250 };
        size = ImageLabel.calculateSize(150, 250, originalImage.getWidth(), originalImage.getHeight(), true);
        break;

    }

    gbl.columnWeights = new double[] { Double.MIN_VALUE };
    gbl.rowWeights = new double[] { Double.MIN_VALUE };
    JPanel imagePanel = new JPanel();
    imagePanel.setLayout(gbl);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 3;
    gbc.insets = new Insets(5, 5, 5, 5);

    JToggleButton button = new JToggleButton();
    button.setBackground(Color.white);
    button.setUI(toggleButtonUI);
    BufferedImage resizedImage = new BufferedImage(size.x, size.y, imageType);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, size.x, size.y, null);
    g.dispose();
    ImageIcon imageIcon = new ImageIcon(resizedImage);
    button.setIcon(imageIcon);
    button.putClientProperty("MediaArtwork", artwork);

    buttonGroup.add(button);
    buttons.add(button);
    imagePanel.add(button, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 5, 0, 0);
    JComboBox cb = new JComboBox(artwork.getImageSizes().toArray());
    button.putClientProperty("MediaArtworkSize", cb);
    imagePanel.add(cb, gbc);

    if (type == ImageType.FANART) {
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 1;
      gbc.anchor = GridBagConstraints.EAST;
      JCheckBox chkbx = new JCheckBox();
      button.putClientProperty("MediaArtworkExtrathumb", chkbx);
      imagePanel.add(chkbx, gbc);
    }

    panelImages.add(imagePanel);
    panelImages.validate();
    panelImages.getParent().validate();

  }

  /**
   * The Class SwingAction_1.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_1 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_1.
     */
    public SwingAction_1() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      task.cancel(true);
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class DownloadTask.
   * 
   * @author Manuel Laggner
   */
  private class DownloadTask extends SwingWorker<Void, Void> {
    /** The tmdb id. */
    private int tmdbId;

    /**
     * Instantiates a new download task.
     * 
     * @param tmdbId
     *          the tmdb id
     */
    public DownloadTask(int tmdbId) {
      this.tmdbId = tmdbId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      if (tmdbId == 0) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("image.download.noid")); //$NON-NLS-1$
        return null;
      }

      startProgressBar(BUNDLE.getString("image.download.progress")); //$NON-NLS-1$

      TmdbMetadataProvider artworkProvider;
      try {
        artworkProvider = new TmdbMetadataProvider();
      }
      catch (Exception e1) {
        LOGGER.warn("can't load artwork provider", e1);
        return null;
      }

      MediaArtworkType artworkType = null;

      switch (type) {
        case FANART:
          artworkType = MediaArtworkType.BACKGROUND;
          break;

        case POSTER:
          artworkType = MediaArtworkType.POSTER;
          break;

        default:
          return null;
      }

      List<MediaArtwork> artwork = null;
      try {
        artwork = artworkProvider.getMovieSetArtwork(tmdbId, artworkType);
      }
      catch (Exception e1) {
        LOGGER.warn("can't load artwork provider", e1);
      }

      // return if nothing has been found
      if (artwork == null) {
        return null;
      }

      // display all images
      for (MediaArtwork art : artwork) {
        if (isCancelled()) {
          return null;
        }

        CachedUrl cachedUrl = null;
        try {
          cachedUrl = new CachedUrl(art.getPreviewUrl());
          Image image = Toolkit.getDefaultToolkit().createImage(cachedUrl.getBytes());
          BufferedImage bufferedImage = com.bric.image.ImageLoader.createImage(image);
          addImage(bufferedImage, art);
        }
        catch (Exception e) {
          LOGGER.error("DownloadTask", e);
          // mark cache file as damaged
          if (cachedUrl != null) {
            cachedUrl.removeCachedFile();
          }
        }
      }

      return null;
    }

    /*
     * Executed in event dispatching thread
     */
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    public void done() {
      stopProgressBar();
    }
  }

  /**
   * The Class SwingAction_2.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_2 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_2.
     */
    public SwingAction_2() {
      putValue(NAME, BUNDLE.getString("image.choose.file")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("image.choose.file")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      // JNativeFileChooser fileChooser = new JNativeFileChooser();
      // FileFilter filter = new ImageFileFilter();
      // fileChooser.setFileFilter(filter);
      // fileChooser.setMultiSelectionEnabled(false);
      // if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
      File file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose")); //$NON-NLS-1$
      if (file != null && file.exists() && file.isFile()) {
        String fileName = file.getPath();

        switch (type) {
          case POSTER:
            imageLabel.setImageUrl("file:/" + fileName);
            break;

          case FANART:
            imageLabel.setImageUrl("file:/" + fileName);
            break;
        }

        task.cancel(true);
        setVisible(false);
        dispose();
      }

    }

    /**
     * The Class ImageFileFilter.
     * 
     * @author Manuel Laggner
     */
    public class ImageFileFilter extends FileFilter {

      /** The Constant serialVersionUID. */
      private static final long serialVersionUID = 1L;

      /** The ok file extensions. */
      private final String[]    okFileExtensions = new String[] { "jpg", "png" };

      /*
       * (non-Javadoc)
       * 
       * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
       */
      public boolean accept(File file) {
        if (file.isDirectory())
          return true;

        for (String extension : okFileExtensions) {
          if (file.getName().toLowerCase().endsWith(extension)) {
            return true;
          }
        }
        return false;
      }

      /*
       * (non-Javadoc)
       * 
       * @see javax.swing.filechooser.FileFilter#getDescription()
       */
      @Override
      public String getDescription() {
        return "image files (.jpg; .png)";
      }
    }
  }
}
