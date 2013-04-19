/*
 * Copyright 2012 - 2013 Manuel Laggner
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.ImageSizeAndUrl;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.ui.ImageLabel;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.ToggleButtonUI;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.WrapLayout;

import com.bric.image.pixel.Scaling;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class ImageChooser.
 * 
 * @author Manuel Laggner
 */
public class ImageChooserDialog extends JDialog {

  /** The Constant BUNDLE. */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID = 1L;

  /** The Constant logger. */
  private static final Logger         LOGGER           = LoggerFactory.getLogger(ImageChooserDialog.class);

  /**
   * The Enum ImageType.
   * 
   * @author Manuel Laggner
   */
  public enum ImageType {

    /** The poster. */
    POSTER,
    /** The fanart. */
    FANART,
    /** The banner. */
    BANNER;
  }

  /** The content panel. */
  private final JPanel                contentPanel   = new JPanel();

  /** The progress bar. */
  private JProgressBar                progressBar;

  /** The lbl progress action. */
  private JLabel                      lblProgressAction;

  /** The panel images. */
  private JPanel                      panelImages;

  /** The image label. */
  private ImageLabel                  imageLabel;

  /** The type. */
  private ImageType                   type;

  /** The button group. */
  private ButtonGroup                 buttonGroup    = new ButtonGroup();

  /** The buttons. */
  private List<JToggleButton>         buttons        = new ArrayList<JToggleButton>();

  /** The task. */
  private DownloadTask                task;

  /** The action ok. */
  private final Action                actionOK       = new SwingAction();

  /** The action cancel. */
  private final Action                actionCancel   = new SwingAction_1();

  /** The toggle button ui. */
  private final ToggleButtonUI        toggleButtonUI = new ToggleButtonUI();

  /** The extra thumbs. */
  private List<String>                extraThumbs;

  /** The extra fanarts. */
  private List<String>                extraFanarts;

  /** The action. */
  private final Action                action         = new SwingAction_2();

  /** The artwork providers. */
  private List<IMediaArtworkProvider> artworkProviders;

  /**
   * Instantiates a new image chooser dialog.
   * 
   * @param ids
   *          the ids
   * @param type
   *          the type
   * @param artworkProviders
   *          the artwork providers
   * @param imageLabel
   *          the image label
   * @param extraThumbs
   *          the extra thumbs
   * @param extraFanarts
   *          the extra fanarts
   */
  public ImageChooserDialog(final HashMap<String, Object> ids, ImageType type, List<IMediaArtworkProvider> artworkProviders, ImageLabel imageLabel,
      List<String> extraThumbs, List<String> extraFanarts) {
    setModal(true);
    setIconImage(Globals.logo);
    this.imageLabel = imageLabel;
    this.type = type;
    this.artworkProviders = artworkProviders;
    this.extraThumbs = extraThumbs;
    this.extraFanarts = extraFanarts;

    switch (type) {
      case FANART:
        setTitle(BUNDLE.getString("image.choose.fanart")); //$NON-NLS-1$
        break;

      case POSTER:
        setTitle(BUNDLE.getString("image.choose.poster")); //$NON-NLS-1$
        break;

      case BANNER:
        setTitle(BUNDLE.getString("image.choose.banner")); //$NON-NLS-1$
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
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"),
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.NARROW_LINE_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
          FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("23px:grow"), }));
      {
        if (type == ImageType.FANART) {
          JPanel panelExtraButtons = new JPanel();
          buttonPane.add(panelExtraButtons, "2, 2, fill, bottom");
          panelExtraButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
          {
            if (Globals.settings.getMovieSettings().isImageExtraThumbs()) {
              JLabel labelThumbs = new JLabel("Extrathumbs:");
              panelExtraButtons.add(labelThumbs);
              JButton btnMarkExtrathumbs = new JButton("");
              btnMarkExtrathumbs.setMargin(new Insets(0, 0, 0, 0));
              btnMarkExtrathumbs.setIcon(new ImageIcon(ImageChooserDialog.class.getResource("/org/tinymediamanager/ui/images/checkall.png")));
              btnMarkExtrathumbs.setToolTipText(BUNDLE.getString("image.extrathumbs.markall")); //$NON-NLS-1$
              btnMarkExtrathumbs.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                  for (JToggleButton button : buttons) {
                    if (button.getClientProperty("MediaArtworkExtrathumb") instanceof JCheckBox) {
                      JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrathumb");
                      chkbx.setSelected(true);
                    }
                  }
                }
              });
              panelExtraButtons.add(btnMarkExtrathumbs);
              JButton btnUnMarkExtrathumbs = new JButton("");
              btnUnMarkExtrathumbs.setMargin(new Insets(0, 0, 0, 0));
              btnUnMarkExtrathumbs.setIcon(new ImageIcon(ImageChooserDialog.class.getResource("/org/tinymediamanager/ui/images/uncheckall.png")));
              btnUnMarkExtrathumbs.setToolTipText(BUNDLE.getString("image.extrathumbs.unmarkall")); //$NON-NLS-1$
              btnUnMarkExtrathumbs.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                  for (JToggleButton button : buttons) {
                    if (button.getClientProperty("MediaArtworkExtrathumb") instanceof JCheckBox) {
                      JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrathumb");
                      chkbx.setSelected(false);
                    }
                  }
                }
              });
              panelExtraButtons.add(btnUnMarkExtrathumbs);
            }
            if (Globals.settings.getMovieSettings().isImageExtraThumbs() && Globals.settings.getMovieSettings().isImageExtraFanart()) {
              JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
              separator.setPreferredSize(new Dimension(2, 16));
              panelExtraButtons.add(separator);
            }
            if (Globals.settings.getMovieSettings().isImageExtraFanart()) {
              JLabel labelFanart = new JLabel("Extrafanart:");
              panelExtraButtons.add(labelFanart);
              JButton btnMarkExtrafanart = new JButton("");
              btnMarkExtrafanart.setMargin(new Insets(0, 0, 0, 0));
              btnMarkExtrafanart.setIcon(new ImageIcon(ImageChooserDialog.class.getResource("/org/tinymediamanager/ui/images/checkall.png")));
              btnMarkExtrafanart.setToolTipText(BUNDLE.getString("image.extrafanart.markall")); //$NON-NLS-1$
              btnMarkExtrafanart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                  for (JToggleButton button : buttons) {
                    if (button.getClientProperty("MediaArtworkExtrafanart") instanceof JCheckBox) {
                      JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrafanart");
                      chkbx.setSelected(true);
                    }
                  }
                }
              });
              panelExtraButtons.add(btnMarkExtrafanart);
              JButton btnUnMarkExtrafanart = new JButton("");
              btnUnMarkExtrafanart.setMargin(new Insets(0, 0, 0, 0));
              btnUnMarkExtrafanart.setIcon(new ImageIcon(ImageChooserDialog.class.getResource("/org/tinymediamanager/ui/images/uncheckall.png")));
              btnUnMarkExtrafanart.setToolTipText(BUNDLE.getString("image.extrafanart.unmarkall")); //$NON-NLS-1$
              btnUnMarkExtrafanart.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                  for (JToggleButton button : buttons) {
                    if (button.getClientProperty("MediaArtworkExtrafanart") instanceof JCheckBox) {
                      JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrafanart");
                      chkbx.setSelected(false);
                    }
                  }
                }
              });
              panelExtraButtons.add(btnUnMarkExtrafanart);
            }
          }
        }
      }

      {
        progressBar = new JProgressBar();
        buttonPane.add(progressBar, "2, 4");
      }
      {
        lblProgressAction = new JLabel("");
        buttonPane.add(lblProgressAction, "4, 4");
      }
      {
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "6, 4, fill, top");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton btnAddFile = new JButton(BUNDLE.getString("Button.addfile")); //$NON-NLS-1$
        btnAddFile.setAction(action);
        buttonPane.add(btnAddFile, "8, 4, fill, top");
      }
      {
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "10, 4, fill, top");
      }
    }

    task = new DownloadTask(ids, this.artworkProviders);
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
              if (cb.getSelectedItem() instanceof ImageSizeAndUrl) {
                resolution = (ImageSizeAndUrl) cb.getSelectedItem();
              }
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

      // extrathumbs
      if (type == ImageType.FANART && Globals.settings.getMovieSettings().isImageExtraThumbs()) {
        processExtraThumbs();
      }

      // extrafanart
      if (type == ImageType.FANART && Globals.settings.getMovieSettings().isImageExtraFanart()) {
        processExtraFanart();
      }

      task.cancel(true);
      setVisible(false);
      dispose();
    }

    /**
     * Process extra thumbs.
     */
    private void processExtraThumbs() {
      extraThumbs.clear();
      // get extrathumbs
      for (JToggleButton button : buttons) {
        if (button.getClientProperty("MediaArtworkExtrathumb") instanceof JCheckBox
            && button.getClientProperty("MediaArtwork") instanceof MediaArtwork && button.getClientProperty("MediaArtworkSize") instanceof JComboBox) {
          JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrathumb");
          if (chkbx.isSelected()) {
            MediaArtwork artwork = (MediaArtwork) button.getClientProperty("MediaArtwork");
            JComboBox cb = (JComboBox) button.getClientProperty("MediaArtworkSize");
            ImageSizeAndUrl size = (ImageSizeAndUrl) cb.getSelectedItem();
            if (size != null) {
              extraThumbs.add(size.getUrl());
            }
            else {
              extraThumbs.add(artwork.getDefaultUrl());
            }
          }
        }
      }
    }

    /**
     * Process extra fanart.
     */
    private void processExtraFanart() {
      extraFanarts.clear();
      // get extrafanart
      for (JToggleButton button : buttons) {
        if (button.getClientProperty("MediaArtworkExtrafanart") instanceof JCheckBox
            && button.getClientProperty("MediaArtwork") instanceof MediaArtwork && button.getClientProperty("MediaArtworkSize") instanceof JComboBox) {
          JCheckBox chkbx = (JCheckBox) button.getClientProperty("MediaArtworkExtrafanart");
          if (chkbx.isSelected()) {
            MediaArtwork artwork = (MediaArtwork) button.getClientProperty("MediaArtwork");
            JComboBox cb = (JComboBox) button.getClientProperty("MediaArtworkSize");
            ImageSizeAndUrl size = (ImageSizeAndUrl) cb.getSelectedItem();
            if (size != null) {
              extraFanarts.add(size.getUrl());
            }
            else {
              extraFanarts.add(artwork.getDefaultUrl());
            }
          }
        }
      }
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
        gbl.columnWidths = new int[] { 130 };
        gbl.rowHeights = new int[] { 180 };
        size = ImageLabel.calculateSize(300, 150, originalImage.getWidth(), originalImage.getHeight(), true);
        break;

      case BANNER:
        gbl.columnWidths = new int[] { 130 };
        gbl.rowHeights = new int[] { 120 };
        size = ImageLabel.calculateSize(300, 100, originalImage.getWidth(), originalImage.getHeight(), true);
        break;

      case POSTER:
      default:
        gbl.columnWidths = new int[] { 180 };
        gbl.rowHeights = new int[] { 270 };
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
    button.setMargin(new Insets(10, 10, 10, 10));
    ImageIcon imageIcon = new ImageIcon(Scaling.scale(originalImage, size.x, size.y));
    button.setIcon(imageIcon);
    button.putClientProperty("MediaArtwork", artwork);

    buttonGroup.add(button);
    buttons.add(button);
    imagePanel.add(button, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.LAST_LINE_START;
    gbc.insets = new Insets(0, 5, 0, 0);
    JComboBox cb = null;
    if (artwork.getImageSizes().size() > 0) {
      cb = new JComboBox(artwork.getImageSizes().toArray());
    }
    else {
      cb = new JComboBox(new String[] { originalImage.getWidth() + "x" + originalImage.getHeight() });
    }
    button.putClientProperty("MediaArtworkSize", cb);
    imagePanel.add(cb, gbc);

    // should we provide an option for extrathumbs
    if (type == ImageType.FANART && Globals.settings.getMovieSettings().isImageExtraThumbs()) {
      gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = 1;
      gbc.anchor = GridBagConstraints.LINE_END;
      JLabel label = new JLabel("Extrathumb");
      imagePanel.add(label, gbc);

      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = 1;
      gbc.anchor = GridBagConstraints.LINE_END;
      JCheckBox chkbx = new JCheckBox();
      button.putClientProperty("MediaArtworkExtrathumb", chkbx);
      imagePanel.add(chkbx, gbc);
    }

    // should we provide an option for extrafanart
    if (type == ImageType.FANART && Globals.settings.getMovieSettings().isImageExtraFanart()) {
      gbc = new GridBagConstraints();
      gbc.gridx = 1;
      gbc.gridy = Globals.settings.getMovieSettings().isImageExtraThumbs() ? 2 : 1;
      gbc.anchor = GridBagConstraints.LINE_END;
      JLabel label = new JLabel("Extrafanart");
      imagePanel.add(label, gbc);

      gbc = new GridBagConstraints();
      gbc.gridx = 2;
      gbc.gridy = Globals.settings.getMovieSettings().isImageExtraThumbs() ? 2 : 1;
      gbc.anchor = GridBagConstraints.LINE_END;
      JCheckBox chkbx = new JCheckBox();
      button.putClientProperty("MediaArtworkExtrafanart", chkbx);
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

    /** The ids. */
    private HashMap<String, Object>     ids;

    /** The artwork providers. */
    private List<IMediaArtworkProvider> artworkProviders;

    /**
     * Instantiates a new download task.
     * 
     * @param ids
     *          the ids
     * @param artworkProviders
     *          the artwork providers
     */
    public DownloadTask(HashMap<String, Object> ids, List<IMediaArtworkProvider> artworkProviders) {
      this.ids = ids;
      this.artworkProviders = artworkProviders;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    public Void doInBackground() {
      if (ids.isEmpty()) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("image.download.noid")); //$NON-NLS-1$
        return null;
      }

      startProgressBar(BUNDLE.getString("image.download.progress")); //$NON-NLS-1$

      try {
        if (artworkProviders == null || artworkProviders.size() == 0) {
          return null;
        }

        // get images from all artworkproviders
        for (IMediaArtworkProvider artworkProvider : artworkProviders) {
          MediaScrapeOptions options = new MediaScrapeOptions();
          switch (type) {
            case POSTER:
              options.setArtworkType(MediaArtworkType.POSTER);
              break;

            case FANART:
              options.setArtworkType(MediaArtworkType.BACKGROUND);
              break;

            case BANNER:
              options.setArtworkType(MediaArtworkType.BANNER);
              break;

          }

          // populate ids
          for (Entry<String, Object> entry : ids.entrySet()) {
            options.setId((String) entry.getKey(), entry.getValue().toString());
          }

          // get the artwork
          List<MediaArtwork> artwork = artworkProvider.getArtwork(options);
          if (artwork == null) {
            continue;
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
        }
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
      File file = TmmUIHelper.selectFile(BUNDLE.getString("image.choose")); //$NON-NLS-1$
      if (file != null && file.exists() && file.isFile()) {
        String fileName = file.getPath();
        imageLabel.setImageUrl("file:/" + fileName);
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
