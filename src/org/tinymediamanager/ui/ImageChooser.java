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
package org.tinymediamanager.ui;

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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.FanartSizes;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork.PosterSizes;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageChooser.
 */
public class ImageChooser extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger LOGGER           = Logger.getLogger(ImageChooser.class);

  /**
   * The Enum ImageType.
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

  /** The extrathumbs. */
  private List<String>         extrathumbs;

  /** The action. */
  private final Action         action         = new SwingAction_2();

  /**
   * Create the dialog.
   * 
   * @param imdbId
   *          the imdb id
   * @param tmdbId
   *          the tmdb id
   * @param type
   *          the type
   * @param imageLabel
   *          the image label
   * @param extrathumbs
   *          the extrathumbs
   */
  public ImageChooser(String imdbId, int tmdbId, ImageType type, ImageLabel imageLabel, List<String> extrathumbs) {
    setModal(true);
    setIconImage(Globals.logo);
    this.imageLabel = imageLabel;
    this.type = type;
    this.extrathumbs = extrathumbs;

    switch (type) {
      case FANART:
        setTitle("Choose fanart");
        break;

      case POSTER:
        setTitle("Choose poster");
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
        JButton okButton = new JButton("OK");
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "6, 2, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton btnAddFile = new JButton("Add file");
        btnAddFile.setAction(action);
        buttonPane.add(btnAddFile, "8, 2, fill, top");
      }
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "10, 2, fill, top");
      }
    }

    task = new DownloadTask(imdbId, tmdbId);
    task.execute();
  }

  /**
   * The Class SwingAction.
   */
  private class SwingAction extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action.
     */
    public SwingAction() {
      putValue(NAME, "OK");
      putValue(SHORT_DESCRIPTION, "Set selected image");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      TmdbArtwork artwork = null;
      PosterSizes posterSize = null;
      FanartSizes fanartSize = null;

      // get selected button
      for (JToggleButton button : buttons) {
        if (button.isSelected()) {
          Object clientProperty = button.getClientProperty("TmdbArtwork");
          if (clientProperty instanceof TmdbArtwork) {
            artwork = (TmdbArtwork) clientProperty;
            clientProperty = button.getClientProperty("TmdbArtworkSize");
            // try to get the size
            if (clientProperty instanceof JComboBox) {
              JComboBox cb = (JComboBox) clientProperty;
              ImageResolution resolution = (ImageResolution) cb.getSelectedItem();
              posterSize = resolution.getPosterSize();
              fanartSize = resolution.getFanartSize();
            }
            break;
          }
        }
      }

      // nothing selected
      if (artwork == null) {
        JOptionPane.showMessageDialog(null, "no image selected!");
        return;
      }

      if (artwork != null) {
        switch (type) {

          case POSTER:
            if (posterSize != null) {
              imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(posterSize));
            }
            else {
              imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(Globals.settings.getImageTmdbPosterSize()));
            }
            break;

          case FANART:
            if (fanartSize != null) {
              imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(fanartSize));
            }
            else {
              imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(Globals.settings.getImageTmdbFanartSize()));
            }
            break;
        }
      }

      // extrathumbs
      if (type == ImageType.FANART) {
        extrathumbs.clear();
        // get extrathumbs
        for (JToggleButton button : buttons) {
          Object clientProperty = button.getClientProperty("TmdbArtworkExtrathumb");
          if (clientProperty instanceof JCheckBox) {
            JCheckBox chkbx = (JCheckBox) clientProperty;
            if (chkbx.isSelected()) {
              clientProperty = button.getClientProperty("TmdbArtwork");
              if (clientProperty instanceof TmdbArtwork) {
                artwork = (TmdbArtwork) clientProperty;
                clientProperty = button.getClientProperty("TmdbArtworkSize");
                // try to get the size
                if (clientProperty instanceof JComboBox) {
                  JComboBox cb = (JComboBox) clientProperty;
                  ImageResolution resolution = (ImageResolution) cb.getSelectedItem();
                  fanartSize = resolution.getFanartSize();

                  if (fanartSize != null) {
                    extrathumbs.add(artwork.getUrlForSpecialArtwork(fanartSize));
                  }
                  else {
                    extrathumbs.add(artwork.getUrlForSpecialArtwork(Globals.settings.getImageTmdbFanartSize()));
                  }
                }
              }
            }
          }
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
   * @param tmdbArtwork
   *          the tmdb artwork
   */
  private void addImage(BufferedImage originalImage, TmdbArtwork tmdbArtwork) {
    int imageType = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
    Point size = null;

    GridBagLayout gbl = new GridBagLayout();
    List<ImageResolution> resolutions = null;

    switch (type) {
      case FANART:
        gbl.columnWidths = new int[] { 300 };
        gbl.rowHeights = new int[] { 150 };
        size = ImageLabel.calculateSize(300, 150, originalImage.getWidth(), originalImage.getHeight(), true);
        resolutions = ImageResolution.getFanartResolutions(tmdbArtwork.getHeight(), tmdbArtwork.getWidth());
        break;

      case POSTER:
      default:
        gbl.columnWidths = new int[] { 150 };
        gbl.rowHeights = new int[] { 250 };
        size = ImageLabel.calculateSize(150, 250, originalImage.getWidth(), originalImage.getHeight(), true);
        resolutions = ImageResolution.getPosterResolutions(tmdbArtwork.getHeight(), tmdbArtwork.getWidth());
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
    button.putClientProperty("TmdbArtwork", tmdbArtwork);

    buttonGroup.add(button);
    buttons.add(button);
    imagePanel.add(button, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 5, 0, 0);
    JComboBox cb = new JComboBox(resolutions.toArray());
    button.putClientProperty("TmdbArtworkSize", cb);
    imagePanel.add(cb, gbc);

    if (type == ImageType.FANART) {
      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 1;
      gbc.anchor = GridBagConstraints.EAST;
      JCheckBox chkbx = new JCheckBox();
      button.putClientProperty("TmdbArtworkExtrathumb", chkbx);
      imagePanel.add(chkbx, gbc);
    }

    panelImages.add(imagePanel);
    panelImages.validate();
    panelImages.getParent().validate();

  }

  /**
   * The Class SwingAction_1.
   */
  private class SwingAction_1 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_1.
     */
    public SwingAction_1() {
      putValue(NAME, "Cancel");
      putValue(SHORT_DESCRIPTION, "Cancel");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      task.cancel(true);
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class DownloadTask.
   */
  private class DownloadTask extends SwingWorker<Void, Void> {

    /** The imdb id. */
    private String imdbId;

    /** The tmdb id. */
    private int    tmdbId;

    /**
     * Instantiates a new download task.
     * 
     * @param imdbId
     *          the imdb id
     * @param tmdbId
     *          the tmdb id
     */
    public DownloadTask(String imdbId, int tmdbId) {
      this.imdbId = imdbId;
      this.tmdbId = tmdbId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      startProgressBar("Downloading images");

      try {
        TmdbMetadataProvider tmdb = new TmdbMetadataProvider();
        List<TmdbArtwork> artwork = null;
        switch (type) {
          case POSTER:
            // poster
            if (tmdbId > 0) {
              // retrieve via TMDB ID
              artwork = tmdb.getArtwork(tmdbId, MediaArtworkType.POSTER);
            }
            else {
              if (!StringUtils.isEmpty(imdbId)) {
                // retrieve via IMDB ID
                artwork = tmdb.getArtwork(imdbId, MediaArtworkType.POSTER);
              }
            }
            break;

          case FANART:
            // fanart
            if (tmdbId > 0) {
              // retrieve via TMDB ID
              artwork = tmdb.getArtwork(tmdbId, MediaArtworkType.BACKGROUND);
            }
            else {
              if (!StringUtils.isEmpty(imdbId)) {
                // retrieve via IMDB ID
                artwork = tmdb.getArtwork(imdbId, MediaArtworkType.BACKGROUND);
              }
            }
            break;
        }

        for (TmdbArtwork tmdbArtwork : artwork) {
          if (isCancelled()) {
            return null;
          }
          CachedUrl cachedUrl = null;
          try{
        	  cachedUrl = new CachedUrl(tmdbArtwork.getUrlForSmallArtwork());
	          Image image = Toolkit.getDefaultToolkit().createImage(cachedUrl.getBytes());
	          BufferedImage bufferedImage = com.bric.image.ImageLoader.createImage(image);
	          addImage(bufferedImage, tmdbArtwork);
          } catch (Exception e){
        	  LOGGER.error("DownloadTask", e);
        	  // ToDo: mark cache file as damaged; maybe retry?
        	  if(cachedUrl != null){
        		cachedUrl.removeCachedFile();
        	  }
          }
        }

      }
      catch (NumberFormatException e) {
        LOGGER.error("DownloadTask", e);
      }
      catch (Exception e) {
        LOGGER.error("DownloadTask", e);
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
   * The Class ImageResolution.
   */
  private static class ImageResolution {

    /** The poster size. */
    private PosterSizes posterSize;

    /** The fanart size. */
    private FanartSizes fanartSize;

    /** The height. */
    int                 height;

    /** The width. */
    int                 width;

    /**
     * Instantiates a new image resolution.
     * 
     * @param posterSize
     *          the poster size
     * @param fullHeight
     *          the full height
     * @param fullWidth
     *          the full width
     */
    private ImageResolution(PosterSizes posterSize, int fullHeight, int fullWidth) {
      this.posterSize = posterSize;
      switch (this.posterSize) {
        case w92:
          this.width = 92;
          break;

        case w154:
          this.width = 154;
          break;

        case w185:
          this.width = 185;
          break;

        case w342:
          this.width = 342;
          break;

        case w500:
          this.width = 500;
          break;

        case original:
          this.width = fullWidth;
          this.height = fullHeight;
          break;
      }

      if (posterSize != PosterSizes.original) {
        calculateHeight(fullHeight, fullWidth);
      }
    }

    /**
     * Instantiates a new image resolution.
     * 
     * @param fanartSize
     *          the fanart size
     * @param fullHeight
     *          the full height
     * @param fullWidth
     *          the full width
     */
    private ImageResolution(FanartSizes fanartSize, int fullHeight, int fullWidth) {
      this.fanartSize = fanartSize;
      switch (this.fanartSize) {
        case w300:
          this.width = 300;
          break;

        case w780:
          this.width = 780;
          break;

        case w1280:
          this.width = 1280;
          break;

        case original:
          this.width = fullWidth;
          this.height = fullHeight;
          break;
      }

      if (fanartSize != FanartSizes.original) {
        calculateHeight(fullHeight, fullWidth);
      }
    }

    /**
     * Calculate height.
     * 
     * @param fullHeight
     *          the full height
     * @param fullWidth
     *          the full width
     */
    private void calculateHeight(int fullHeight, int fullWidth) {
      this.height = fullHeight * this.width / fullWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
      return new String(this.width + "x" + this.height);
    }

    /**
     * Gets the poster size.
     * 
     * @return the poster size
     */
    public PosterSizes getPosterSize() {
      return this.posterSize;
    }

    /**
     * Gets the fanart size.
     * 
     * @return the fanart size
     */
    public FanartSizes getFanartSize() {
      return this.fanartSize;
    }

    /**
     * Gets the poster resolutions.
     * 
     * @param fullHeight
     *          the full height
     * @param fullWidth
     *          the full width
     * @return the poster resolutions
     */
    public static List<ImageResolution> getPosterResolutions(int fullHeight, int fullWidth) {
      List<ImageResolution> resolutions = new ArrayList<ImageChooser.ImageResolution>();
      for (PosterSizes size : PosterSizes.values()) {
        // default should be the first in line
        if (size == Globals.settings.getImageTmdbPosterSize()) {
          resolutions.add(0, new ImageResolution(size, fullHeight, fullWidth));
        }
        else {
          resolutions.add(new ImageResolution(size, fullHeight, fullWidth));
        }
      }
      return resolutions;
    }

    /**
     * Gets the fanart resolutions.
     * 
     * @param fullHeight
     *          the full height
     * @param fullWidth
     *          the full width
     * @return the fanart resolutions
     */
    public static List<ImageResolution> getFanartResolutions(int fullHeight, int fullWidth) {
      List<ImageResolution> resolutions = new ArrayList<ImageChooser.ImageResolution>();
      for (FanartSizes size : FanartSizes.values()) {
        // default should be the first in line
        if (size == Globals.settings.getImageTmdbFanartSize()) {
          resolutions.add(0, new ImageResolution(size, fullHeight, fullWidth));
        }
        else {
          resolutions.add(new ImageResolution(size, fullHeight, fullWidth));
        }
      }
      return resolutions;
    }
  }

  /**
   * The Class SwingAction_2.
   */
  private class SwingAction_2 extends AbstractAction {

    /**
     * Instantiates a new swing action_2.
     */
    public SwingAction_2() {
      putValue(NAME, "Choose file");
      putValue(SHORT_DESCRIPTION, "Choose a file");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      JFileChooser fileChooser = new JFileChooser();
      FileFilter filter = new ImageFileFilter();
      fileChooser.setFileFilter(filter);
      fileChooser.setMultiSelectionEnabled(false);
      if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        String fileName = fileChooser.getSelectedFile().getPath();

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
     */
    public class ImageFileFilter extends FileFilter {

      /** The ok file extensions. */
      private final String[] okFileExtensions = new String[] { "jpg", "png" };

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
