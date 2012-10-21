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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class ImageChooser.
 */
public class ImageChooser extends JDialog {

  /** The Constant logger. */
  private static final Logger LOGGER = Logger.getLogger(ImageChooser.class);

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
  private final JPanel        contentPanel = new JPanel();

  /** The progress bar. */
  private JProgressBar        progressBar;

  /** The lbl progress action. */
  private JLabel              lblProgressAction;

  /** The panel images. */
  private JPanel              panelImages;

  /** The image label. */
  private ImageLabel          imageLabel;

  /** The type. */
  private ImageType           type;

  /** The button group. */
  private ButtonGroup         buttonGroup  = new ButtonGroup();

  /** The buttons. */
  private List<JToggleButton> buttons      = new ArrayList<JToggleButton>();

  /** The task. */
  private DownloadTask        task;

  /** The action ok. */
  private final Action        actionOK     = new SwingAction();

  /** The action cancel. */
  private final Action        actionCancel = new SwingAction_1();

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
   */
  public ImageChooser(String imdbId, int tmdbId, ImageType type, ImageLabel imageLabel) {
    setModal(true);
    this.imageLabel = imageLabel;
    this.type = type;

    setBounds(5, 5, 968, 590);
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
          FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
          FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("23px"), }));
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
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "8, 2, fill, top");
      }
    }

    task = new DownloadTask(imdbId, tmdbId);
    task.execute();
  }

  /**
   * The Class SwingAction.
   */
  private class SwingAction extends AbstractAction {

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
      // get selected button
      for (JToggleButton button : buttons) {
        if (button.isSelected()) {
          Object clientProperty = button.getClientProperty("TmdbArtwork");
          if (clientProperty instanceof TmdbArtwork) {
            artwork = (TmdbArtwork) clientProperty;
            break;
          }
        }
      }

      if (artwork != null) {
        switch (type) {

          case POSTER:
            imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(Globals.settings.getImageTmdbPosterSize()));
            break;

          case FANART:
            imageLabel.setImageUrl(artwork.getUrlForSpecialArtwork(Globals.settings.getImageTmdbFanartSize()));
            break;
        }
      }
      task.cancel(true);
      setVisible(false);
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
    gbc.insets = new Insets(5, 5, 5, 5);

    JToggleButton button = new JToggleButton();
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

    panelImages.add(imagePanel);
    panelImages.validate();
    panelImages.getParent().validate();

  }

  /**
   * The Class SwingAction_1.
   */
  private class SwingAction_1 extends AbstractAction {

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

      TmdbMetadataProvider tmdb = TmdbMetadataProvider.getInstance();
      try {
        List<TmdbArtwork> artwork = null;
        switch (type) {
          case POSTER:
            // poster
            if (tmdbId > 0) {
              // retrieve via TMDB ID
              artwork = tmdb.getArtwork(tmdbId, MediaArtifactType.POSTER);
            }
            if (!StringUtils.isEmpty(imdbId)) {
              // retrieve via IMDB ID
              artwork = tmdb.getArtwork(imdbId, MediaArtifactType.POSTER);
            }
            break;

          case FANART:
            // fanart
            if (tmdbId > 0) {
              // retrieve via TMDB ID
              artwork = tmdb.getArtwork(tmdbId, MediaArtifactType.BACKGROUND);
            }
            if (!StringUtils.isEmpty(imdbId)) {
              // retrieve via IMDB ID
              artwork = tmdb.getArtwork(imdbId, MediaArtifactType.BACKGROUND);
            }
            break;
        }

        for (TmdbArtwork tmdbArtwork : artwork) {
          if (isCancelled()) {
            return null;
          }
          CachedUrl cachedUrl = new CachedUrl(tmdbArtwork.getUrlForSmallArtwork());
          Image image = Toolkit.getDefaultToolkit().createImage(cachedUrl.getBytes());
          BufferedImage bufferedImage = com.bric.image.ImageLoader.createImage(image);

          // // performance of image loading isnt a problem here, so we use
          // // ImageIO.read (so we can use HttpClient for faster network
          // // performance
          // BufferedImage bufferedImage =
          // ImageIO.read(cachedUrl.getInputStream());

          addImage(bufferedImage, tmdbArtwork);
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
}
