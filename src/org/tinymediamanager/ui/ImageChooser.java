package org.tinymediamanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.tmdb.TmdbArtwork;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImageChooser extends JDialog {

  public enum ImageType {
    POSTER, FANART
  }

  private final JPanel contentPanel = new JPanel();
  private JProgressBar progressBar;
  private JLabel lblProgressAction;
  private JPanel panelImages;

  private ImageLabel imageLabel;
  private ImageType type;

  private ButtonGroup buttonGroup = new ButtonGroup();
  private List<JToggleButton> buttons = new ArrayList<JToggleButton>();
  private DownloadTask task;

  private final Action actionOK = new SwingAction();
  private final Action actionCancel = new SwingAction_1();

  /**
   * Create the dialog.
   */
  public ImageChooser(String imdbId, int tmdbId, ImageType type, ImageLabel imageLabel) {
    setModal(true);
    this.imageLabel = imageLabel;
    this.type = type;

    setBounds(100, 100, 968, 590);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("258px:grow"), }, new RowSpec[] {
        FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("fill:266px:grow"), }));
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
      buttonPane.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.RELATED_GAP_COLSPEC,
          ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("100px"),
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
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton, "8, 2, fill, top");
      }
    }

    task = new DownloadTask(imdbId, tmdbId);
    task.execute();
  }

  private class SwingAction extends AbstractAction {
    public SwingAction() {
      putValue(NAME, "OK");
      putValue(SHORT_DESCRIPTION, "Set selected image");
    }

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

  private void startProgressBar(String description) {
    lblProgressAction.setText(description);
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
  }

  private void stopProgressBar() {
    lblProgressAction.setText("");
    progressBar.setVisible(false);
    progressBar.setIndeterminate(false);
  }

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

  private class SwingAction_1 extends AbstractAction {
    public SwingAction_1() {
      putValue(NAME, "Cancel");
      putValue(SHORT_DESCRIPTION, "Cancel");
    }

    public void actionPerformed(ActionEvent e) {
      task.cancel(true);
      setVisible(false);
    }
  }

  private class DownloadTask extends SwingWorker<Void, Void> {

    private String imdbId;
    private int tmdbId;

    public DownloadTask(String imdbId, int tmdbId) {
      this.imdbId = imdbId;
      this.tmdbId = tmdbId;
    }

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
          if (task.isCancelled()) {
            return null;
          }
          CachedUrl cachedUrl = new CachedUrl(tmdbArtwork.getUrlForSmallArtwork());
          BufferedImage bufferedImage = ImageIO.read(cachedUrl.getInputStream(null, true));
          addImage(bufferedImage, tmdbArtwork);
        }

      } catch (NumberFormatException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      return null;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
      stopProgressBar();
    }
  }
}
