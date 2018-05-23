/*
 * Copyright 2012 - 2018 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.dialogs;

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.ShadowLayerUI;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MainTabbedPane;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The Class TvShowSeasonEditor.
 * 
 * @author Manuel Laggner
 */
public class TvShowSeasonEditorDialog extends TmmDialog {
  private static final long serialVersionUID = 3270218410302989845L;

  private TvShowSeason      tvShowSeasonToEdit;
  private TvShowList        tvShowList       = TvShowList.getInstance();

  private boolean           continueQueue    = true;
  private boolean           navigateBack     = false;
  private int               queueIndex;
  private int               queueSize;

  /**
   * UI elements
   */
  private ImageLabel        lblPoster;
  private ImageLabel        lblBanner;
  private ImageLabel        lblThumb;

  private JTextField        tfPoster;
  private JTextField        tfBanner;
  private JTextField        tfThumb;

  /**
   * Instantiates a new tv show season editor dialog.
   *
   * @param tvShowSeason
   *          the tv show season
   * @param queueIndex
   *          the actual index in the queue
   * @param queueSize
   *          the queue size
   */
  public TvShowSeasonEditorDialog(TvShowSeason tvShowSeason, int queueIndex, int queueSize) {
    super(BUNDLE.getString("tvshowseason.edit") + (queueSize > 1 ? " " + (queueIndex + 1) + "/" + queueSize : ""), "tvShowSeasonEditor"); //$NON-NLS-1$

    this.tvShowSeasonToEdit = tvShowSeason;
    this.queueIndex = queueIndex;
    this.queueSize = queueSize;

    initComponents();

    {
      lblPoster.setImagePath(tvShowSeason.getArtworkFilename(SEASON_POSTER));
      lblThumb.setImagePath(tvShowSeason.getArtworkFilename(SEASON_THUMB));
      lblBanner.setImagePath(tvShowSeason.getArtworkFilename(SEASON_BANNER));

      tfPoster.setText(tvShowSeason.getArtworkUrl(SEASON_POSTER));
      tfThumb.setText(tvShowSeason.getArtworkUrl(SEASON_THUMB));
      tfBanner.setText(tvShowSeason.getArtworkUrl(SEASON_BANNER));
    }
  }

  private void initComponents() {
    JTabbedPane tabbedPane = new MainTabbedPane() {
      private static final long serialVersionUID = 71548865608767532L;

      @Override
      public void updateUI() {
        putClientProperty("bottomBorder", Boolean.FALSE);
        super.updateUI();
      }
    };

    // to draw the shadow beneath window frame, encapsulate the panel
    JLayer<JComponent> rootLayer = new JLayer(tabbedPane, new ShadowLayerUI()); // removed <> because this leads WBP to crash
    getContentPane().add(rootLayer, BorderLayout.CENTER);

    /**********************************************************************************
     * local artwork pane
     **********************************************************************************/
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null);
      artworkPanel.setLayout(new MigLayout("", "[200lp:300lp,grow][20lp:n][200lp:300lp,grow]", "[][100lp:125lp,grow][20lp:n][][100lp:125lp,grow]"));
      {
        JLabel lblPosterT = new TmmLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
        artworkPanel.add(lblPosterT, "cell 0 0");

        lblPoster = new ImageLabel();
        lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblPoster.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            Map<String, Object> ids = tvShowSeasonToEdit.getTvShow().getIds();
            ids.put("tvShowSeason", tvShowSeasonToEdit.getSeason());
            ImageChooserDialog dialog = new ImageChooserDialog(TvShowSeasonEditorDialog.this, ids, ImageType.SEASON_POSTER,
                tvShowList.getAvailableArtworkScrapers(), lblPoster, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblPoster, tfPoster);
          }
        });

        artworkPanel.add(lblPoster, "cell 0 1,grow");
      }
      {
        JLabel lblThumbT = new JLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
        artworkPanel.add(lblThumbT, "cell 2 0");

        lblThumb = new ImageLabel();
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            Map<String, Object> ids = tvShowSeasonToEdit.getTvShow().getIds();
            ids.put("tvShowSeason", tvShowSeasonToEdit.getSeason());
            ImageChooserDialog dialog = new ImageChooserDialog(TvShowSeasonEditorDialog.this, ids, ImageType.SEASON_THUMB,
                tvShowList.getAvailableArtworkScrapers(), lblThumb, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblThumb, tfThumb);
          }
        });

        artworkPanel.add(lblThumb, "cell 2 1,grow");
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
        artworkPanel.add(lblBannerT, "cell 0 3");

        lblBanner = new ImageLabel();
        lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblBanner.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            Map<String, Object> ids = tvShowSeasonToEdit.getTvShow().getIds();
            ids.put("tvShowSeason", tvShowSeasonToEdit.getSeason());
            ImageChooserDialog dialog = new ImageChooserDialog(TvShowSeasonEditorDialog.this, ids, ImageType.SEASON_BANNER,
                tvShowList.getAvailableArtworkScrapers(), lblBanner, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
            updateArtworkUrl(lblBanner, tfBanner);
          }
        });
        artworkPanel.add(lblBanner, "cell 0 4 3 1,grow");
      }
    }

    /**********************************************************************************
     * artwork urls
     **********************************************************************************/
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("edit.artwork"), null, artworkPanel, null);
      artworkPanel.setLayout(new MigLayout("", "[][grow]", "[][][]"));
      {
        JLabel lblPosterT = new TmmLabel(BUNDLE.getString("mediafiletype.poster")); //$NON-NLS-1$
        artworkPanel.add(lblPosterT, "cell 0 0,alignx right");

        tfPoster = new JTextField();
        artworkPanel.add(tfPoster, "cell 1 0,growx");
      }
      {
        JLabel lblBannerT = new TmmLabel(BUNDLE.getString("mediafiletype.banner")); //$NON-NLS-1$
        artworkPanel.add(lblBannerT, "cell 0 1,alignx right");

        tfBanner = new JTextField();
        artworkPanel.add(tfBanner, "cell 1 1,growx");
      }
      {
        JLabel lblThumbT = new TmmLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
        artworkPanel.add(lblThumbT, "cell 0 2,alignx right");

        tfThumb = new JTextField();
        artworkPanel.add(tfThumb, "cell 1 2,growx");
      }
    }

    /**********************************************************************************
     * button pane
     **********************************************************************************/
    {
      if (queueSize > 1) {
        JButton btnAbort = new JButton(new AbortAction());
        addButton(btnAbort);
        if (queueIndex > 0) {
          JButton backButton = new JButton(new NavigateBackAction());
          addButton(backButton);
        }
      }

      JButton cancelButton = new JButton(new CancelAction());
      addButton(cancelButton);

      JButton okButton = new JButton(new OKAction());
      addDefaultButton(okButton);
    }
  }

  private void updateArtworkUrl(ImageLabel imageLabel, JTextField textField) {
    if (StringUtils.isNotBlank(imageLabel.getImageUrl())) {
      textField.setText(imageLabel.getImageUrl());
    }
  }

  private class OKAction extends AbstractAction {
    private static final long serialVersionUID = 6699599213348390696L;

    OKAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (!StringUtils.isEmpty(tfPoster.getText()) && !tfPoster.getText().equals(tvShowSeasonToEdit.getArtworkUrl(SEASON_POSTER))) {
        tvShowSeasonToEdit.setArtworkUrl(tfPoster.getText(), SEASON_POSTER);
        tvShowSeasonToEdit.downloadArtwork(SEASON_POSTER);
      }

      if (!StringUtils.isEmpty(tfBanner.getText()) && !tfBanner.getText().equals(tvShowSeasonToEdit.getArtworkUrl(SEASON_BANNER))) {
        tvShowSeasonToEdit.setArtworkUrl(tfBanner.getText(), SEASON_BANNER);
        tvShowSeasonToEdit.downloadArtwork(SEASON_BANNER);
      }

      if (!StringUtils.isEmpty(tfThumb.getText()) && !tfThumb.getText().equals(tvShowSeasonToEdit.getArtworkUrl(SEASON_THUMB))) {
        tvShowSeasonToEdit.setArtworkUrl(tfThumb.getText(), SEASON_THUMB);
        tvShowSeasonToEdit.downloadArtwork(SEASON_THUMB);
      }

      tvShowSeasonToEdit.getTvShow().saveToDb();
      setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -4617793684152607277L;

    CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class NavigateBackAction extends AbstractAction {
    private static final long serialVersionUID = -1652218154720642310L;

    public NavigateBackAction() {
      putValue(NAME, BUNDLE.getString("Button.back")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.BACK_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      navigateBack = true;
      setVisible(false);
    }
  }

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }

  public boolean isContinueQueue() {
    return continueQueue;
  }

  public boolean isNavigateBack() {
    return navigateBack;
  }

  private class AbortAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    AbortAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.STOP_INV);
      putValue(LARGE_ICON_KEY, IconManager.STOP_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }
}
