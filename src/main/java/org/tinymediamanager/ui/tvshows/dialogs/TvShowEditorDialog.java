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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.trakttv.SyncTraktTvTask;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TableColumnResizer;
import org.tinymediamanager.ui.TableSpinnerEditor;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.components.MediaIdTable;
import org.tinymediamanager.ui.components.MediaIdTable.MediaId;
import org.tinymediamanager.ui.components.datepicker.DatePicker;
import org.tinymediamanager.ui.components.datepicker.YearSpinner;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

/**
 * The Class TvShowEditor.
 * 
 * @author Manuel Laggner
 */
public class TvShowEditorDialog extends TmmDialog {
  private static final long                                                                       serialVersionUID = 3270218410302989845L;
  /** @wbp.nls.resourceBundle messages */
  private final static ResourceBundle                                                             BUNDLE           = ResourceBundle
      .getBundle("messages", new UTF8Control());                                                                                              //$NON-NLS-1$

  private TvShow                                                                                  tvShowToEdit;
  private TvShowList                                                                              tvShowList       = TvShowList.getInstance();
  private List<TvShowActor>                                                                       actors           = ObservableCollections
      .observableList(new ArrayList<TvShowActor>());
  private List<MediaGenres>                                                                       genres           = ObservableCollections
      .observableList(new ArrayList<MediaGenres>());
  private EventList<MediaId>                                                                      ids              = new BasicEventList<>();
  private List<String>                                                                            tags             = ObservableCollections
      .observableList(new ArrayList<String>());
  private List<TvShowEpisodeEditorContainer>                                                      episodes         = ObservableCollections
      .observableList(new ArrayList<TvShowEpisodeEditorContainer>());
  private boolean                                                                                 continueQueue    = true;

  /**
   * UI elements
   */
  private final JPanel                                                                            details1Panel    = new JPanel();
  private final JPanel                                                                            details2Panel    = new JPanel();
  private final JPanel                                                                            episodesPanel    = new JPanel();
  private JTextField                                                                              tfTitle;
  private YearSpinner                                                                             spYear;
  private JTextPane                                                                               tpPlot;
  private JTable                                                                                  tableActors;
  private JLabel                                                                                  lvlTvShowPath;
  private ImageLabel                                                                              lblPoster;
  private ImageLabel                                                                              lblFanart;
  private ImageLabel                                                                              lblBanner;
  private JSpinner                                                                                spRuntime;
  private JTextField                                                                              tfStudio;
  private JList<MediaGenres>                                                                      listGenres;
  private AutocompleteComboBox<MediaGenres>                                                       cbGenres;
  private AutoCompleteSupport<MediaGenres>                                                        cbGenresAutoCompleteSupport;
  private JSpinner                                                                                spRating;
  private JComboBox                                                                               cbCertification;
  private JComboBox                                                                               cbStatus;
  // private JTable tableTrailer;
  private AutocompleteComboBox<String>                                                            cbTags;
  private AutoCompleteSupport<String>                                                             cbTagsAutoCompleteSupport;
  private JList<String>                                                                           listTags;
  private JSpinner                                                                                spDateAdded;
  private DatePicker                                                                              dpPremiered;
  private JTable                                                                                  tableEpisodes;
  private JTextField                                                                              tfSorttitle;
  private ImageLabel                                                                              lblLogo;
  private ImageLabel                                                                              lblClearlogo;
  private ImageLabel                                                                              lblClearart;
  private ImageLabel                                                                              lblThumb;

  private JTableBinding<TvShowActor, List<TvShowActor>, JTable>                                   jTableBinding;
  private JListBinding<MediaGenres, List<MediaGenres>, JList>                                     jListBinding;
  private JListBinding<String, List<String>, JList>                                               jListBinding_1;
  private JTableBinding<TvShowEpisodeEditorContainer, List<TvShowEpisodeEditorContainer>, JTable> jTableBinding_2;
  private JTable                                                                                  tableIds;

  /**
   * Instantiates a new tv show editor dialog.
   * 
   * @param tvShow
   *          the tv show
   * @param inQueue
   *          the in queue
   */
  public TvShowEditorDialog(TvShow tvShow, boolean inQueue) {
    super(BUNDLE.getString("tvshow.edit"), "tvShowEditor"); //$NON-NLS-1$
    setBounds(5, 5, 950, 700);

    tvShowToEdit = tvShow;
    ids = MediaIdTable.convertIdMapToEventList(tvShowToEdit.getIds());

    getContentPane().setLayout(new BorderLayout());
    {
      JPanel panelPath = new JPanel();
      getContentPane().add(panelPath, BorderLayout.NORTH);
      panelPath.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.DEFAULT_COLSPEC,
              FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblTvShowPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      panelPath.add(lblTvShowPathT, "2, 2, left, top");

      lvlTvShowPath = new JLabel("");
      TmmFontHelper.changeFont(lblTvShowPathT, 1.166, Font.BOLD);
      panelPath.add(lvlTvShowPath, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    tabbedPane.addTab(BUNDLE.getString("metatag.details"), details1Panel); //$NON-NLS-1$
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    details1Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details1Panel.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("50px:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("30dlu"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("250px:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("15dlu:grow"),
            FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(30dlu;default)"), FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("20dlu:grow"),
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormSpecs.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow(2)"), }));

    {
      JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
      details1Panel.add(lblTitle, "2, 2, right, default");
    }
    {
      tfTitle = new JTextField();
      details1Panel.add(tfTitle, "4, 2, 15, 1, fill, default");
      tfTitle.setColumns(10);
    }
    {
      lblPoster = new ImageLabel();
      lblPoster.setAlternativeText(BUNDLE.getString("image.notfound.poster")); //$NON-NLS-1$
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.POSTER, tvShowList.getAvailableArtworkScrapers(),
              lblPoster, null, null, MediaType.TV_SHOW);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      details1Panel.add(lblPoster, "22, 2, 3, 19, fill, fill");
    }
    {
      JLabel lblSortTitle = new JLabel(BUNDLE.getString("metatag.sorttitle")); //$NON-NLS-1$
      details1Panel.add(lblSortTitle, "2, 4, right, default");
    }
    {
      tfSorttitle = new JTextField();
      details1Panel.add(tfSorttitle, "4, 4, 15, 1, fill, default");
      tfSorttitle.setColumns(10);
    }
    {
      JLabel lblYear = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
      details1Panel.add(lblYear, "2, 6, right, default");
    }
    {
      spYear = new YearSpinner();
      details1Panel.add(spYear, "4, 6, fill, top");
    }
    {
      JLabel lblpremiered = new JLabel(BUNDLE.getString("metatag.premiered")); //$NON-NLS-1$
      details1Panel.add(lblpremiered, "8, 6, right, default");
    }
    {
      dpPremiered = new DatePicker(tvShow.getFirstAired());
      details1Panel.add(dpPremiered, "10, 6, fill, default");
    }
    {
      JLabel lblRuntime = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
      details1Panel.add(lblRuntime, "14, 6, right, default");
    }
    {
      spRuntime = new JSpinner();
      details1Panel.add(spRuntime, "16, 6, fill, default");
    }
    spRuntime.setValue(tvShow.getRuntime());

    {
      JLabel lblMin = new JLabel(BUNDLE.getString("metatag.minutes")); //$NON-NLS-1$
      details1Panel.add(lblMin, "18, 6");
    }
    {
      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      details1Panel.add(lblRating, "2, 8, right, default");
    }
    {
      spRating = new JSpinner();
      details1Panel.add(spRating, "4, 8");
    }
    spRating.setModel(new SpinnerNumberModel(tvShow.getRating(), 0.0, 10.0, 0.1));
    {
      {
        JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
        details1Panel.add(lblCertification, "8, 8, right, default");
      }
    }
    cbCertification = new JComboBox();
    for (Certification cert : Certification.getCertificationsforCountry(TvShowModuleManager.SETTINGS.getCertificationCountry())) {
      cbCertification.addItem(cert);
    }
    details1Panel.add(cbCertification, "10, 8, fill, default");
    cbCertification.setSelectedItem(tvShow.getCertification());
    {
      JLabel lblStatus = new JLabel(BUNDLE.getString("metatag.status")); //$NON-NLS-1$
      details1Panel.add(lblStatus, "14, 8, right, default");
    }
    {
      cbStatus = new JComboBox(new String[] { "", "Continuing", "Ended" });
      details1Panel.add(cbStatus, "16, 8, 3, 1, fill, default");
    }
    cbStatus.setSelectedItem(tvShow.getStatus());
    {
      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      details1Panel.add(lblDateAdded, "2, 10, right, default");
    }
    {
      spDateAdded = new JSpinner(new SpinnerDateModel());
      details1Panel.add(spDateAdded, "4, 10");
    }

    {
      JLabel lblIds = new JLabel("Ids");
      details1Panel.add(lblIds, "2, 12, right, default");
    }
    {
      JScrollPane scrollPaneIds = new JScrollPane();
      details1Panel.add(scrollPaneIds, "4, 12, 9, 5, fill, fill");
      {
        tableIds = new MediaIdTable(ids, ScraperType.TV_SHOW);
        scrollPaneIds.setViewportView(tableIds);
      }
    }
    {
      JButton btnAddId = new JButton("");
      btnAddId.setAction(new AddIdAction());
      btnAddId.setIcon(IconManager.LIST_ADD);
      btnAddId.setMargin(new Insets(2, 2, 2, 2));
      details1Panel.add(btnAddId, "2, 14, right, top");
    }
    {
      JButton btnRemoveId = new JButton("RemoveId");
      btnRemoveId.setAction(new RemoveIdAction());
      btnRemoveId.setIcon(IconManager.LIST_REMOVE);
      btnRemoveId.setMargin(new Insets(2, 2, 2, 2));
      details1Panel.add(btnRemoveId, "2, 16, right, top");
    }
    {
      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      details1Panel.add(lblPlot, "2, 18, right, top");
    }
    {
      JScrollPane scrollPanePlot = new JScrollPane();
      details1Panel.add(scrollPanePlot, "4, 18, 15, 3, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPanePlot.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblStudio = new JLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
      details1Panel.add(lblStudio, "2, 22, right, top");
    }
    {
      tfStudio = new JTextField();
      details1Panel.add(tfStudio, "4, 22, 15, 1");
    }

    /**
     * DetailsPanel 2
     */
    tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel); //$NON-NLS-1$
    details2Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details2Panel.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"), FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("100px:grow(2)"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"), }));
    {
      JLabel lblActors = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
      details2Panel.add(lblActors, "2, 2, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      details2Panel.add(scrollPane, "4, 2, 1, 7");
      {
        tableActors = new JTable();
        tableActors.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        scrollPane.setViewportView(tableActors);
      }
    }
    {
      JLabel lblGenres = new JLabel(BUNDLE.getString("metatag.genre")); //$NON-NLS-1$
      details2Panel.add(lblGenres, "6, 2");
    }
    {
      JButton btnAddActor = new JButton("Add Actor");
      btnAddActor.setMargin(new Insets(2, 2, 2, 2));
      btnAddActor.setAction(new AddActorAction());
      btnAddActor.setIcon(IconManager.LIST_ADD);
      details2Panel.add(btnAddActor, "2, 4, right, top");
    }
    {
      JScrollPane scrollPaneGenres = new JScrollPane();
      details2Panel.add(scrollPaneGenres, "8, 2, 1, 5");
      {
        listGenres = new JList<MediaGenres>();
        scrollPaneGenres.setViewportView(listGenres);
      }
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(new AddGenreAction());
      btnAddGenre.setIcon(IconManager.LIST_ADD);
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddGenre, "6, 4, right, top");
    }
    {
      JButton btnRemoveActor = new JButton(BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(new RemoveActorAction());
      btnRemoveActor.setIcon(IconManager.LIST_REMOVE);
      details2Panel.add(btnRemoveActor, "2,6, right, top");
    }

    {
      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setAction(new RemoveGenreAction());
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.setIcon(IconManager.LIST_REMOVE);
      details2Panel.add(btnRemoveGenre, "6, 6, right, top");
    }
    {
      cbGenres = new AutocompleteComboBox<MediaGenres>(MediaGenres.values());
      cbGenresAutoCompleteSupport = cbGenres.getAutoCompleteSupport();
      InputMap im = cbGenres.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      cbGenres.getActionMap().put(enterAction, new AddGenreAction());
      details2Panel.add(cbGenres, "8,8");
    }
    {
      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      details2Panel.add(lblTags, "2, 10, right, default");
    }
    {
      JScrollPane scrollPaneTags = new JScrollPane();
      details2Panel.add(scrollPaneTags, "4, 10, 1, 5");
      listTags = new JList<String>();
      scrollPaneTags.setViewportView(listTags);
    }
    {
      JButton btnAddTag = new JButton("");
      btnAddTag.setAction(new AddTagAction());
      btnAddTag.setIcon(IconManager.LIST_ADD);
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTag, "2, 12, right, top");
    }
    {
      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setAction(new RemoveTagAction());
      btnRemoveTag.setIcon(IconManager.LIST_REMOVE);
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTag, "2, 14, right, top");
    }
    {
      cbTags = new AutocompleteComboBox<String>(tvShowList.getTagsInTvShows());
      cbTagsAutoCompleteSupport = cbTags.getAutoCompleteSupport();
      InputMap im = cbTags.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      Object enterAction = im.get(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
      cbTags.getActionMap().put(enterAction, new AddTagAction());
      details2Panel.add(cbTags, "4, 16");
    }

    /**
     * extra artwork pane
     */
    {
      JPanel artworkPanel = new JPanel();
      tabbedPane.addTab(BUNDLE.getString("metatag.extraartwork"), null, artworkPanel, null); //$NON-NLS-1$
      artworkPanel.setLayout(new FormLayout(
          new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC,
              ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, },
          new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
              RowSpec.decode("50px:grow(2)"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
              RowSpec.decode("200px:grow(2)"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));
      {
        JLabel lblLogoT = new JLabel(BUNDLE.getString("mediafiletype.logo")); //$NON-NLS-1$
        artworkPanel.add(lblLogoT, "2, 2");
      }
      {
        lblLogo = new ImageLabel();
        lblLogo.setAlternativeText(BUNDLE.getString("image.notfound.logo")); //$NON-NLS-1$
        lblLogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.LOGO, tvShowList.getAvailableArtworkScrapers(),
                lblLogo, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        {
          final JLabel lblClearlogoT = new JLabel(BUNDLE.getString("mediafiletype.clearlogo")); //$NON-NLS-1$
          artworkPanel.add(lblClearlogoT, "4, 2");
        }
        lblLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblLogo, "2, 4, fill, fill");
      }
      {
        lblClearlogo = new ImageLabel();
        lblClearlogo.setAlternativeText(BUNDLE.getString("image.notfound.clearlogo")); //$NON-NLS-1$
        lblClearlogo.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.CLEARLOGO, tvShowList.getAvailableArtworkScrapers(),
                lblClearlogo, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblClearlogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearlogo, "4, 4, fill, fill");
      }
      {
        JLabel lblClearartT = new JLabel(BUNDLE.getString("mediafiletype.clearart")); //$NON-NLS-1$
        artworkPanel.add(lblClearartT, "2, 6");
      }
      {
        lblClearart = new ImageLabel();
        lblClearart.setAlternativeText(BUNDLE.getString("image.notfound.clearart")); //$NON-NLS-1$
        lblClearart.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.CLEARART, tvShowList.getAvailableArtworkScrapers(),
                lblClearart, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblClearart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblClearart, "2, 8, fill, fill");
      }
      {
        JLabel lblThumbT = new JLabel(BUNDLE.getString("mediafiletype.thumb")); //$NON-NLS-1$
        artworkPanel.add(lblThumbT, "4, 6");
      }
      {
        lblThumb = new ImageLabel();
        lblThumb.setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
        lblThumb.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.THUMB, tvShowList.getAvailableArtworkScrapers(),
                lblThumb, null, null, MediaType.TV_SHOW);
            dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
            dialog.setVisible(true);
          }
        });
        lblThumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        artworkPanel.add(lblThumb, "4, 8, fill, fill");
      }

    }
    tabbedPane.addTab(BUNDLE.getString("metatag.episodes"), episodesPanel); //$NON-NLS-1$
    episodesPanel.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("default:grow"), }));
    {
      JButton btnCloneEpisode = new JButton("");
      btnCloneEpisode.setAction(new CloneEpisodeAction());
      episodesPanel.add(btnCloneEpisode, "2, 2");
    }
    {
      JScrollPane scrollPaneEpisodes = new JScrollPane();
      episodesPanel.add(scrollPaneEpisodes, "4, 2, 1, 3, fill, fill");
      {
        tableEpisodes = new JTable();
        scrollPaneEpisodes.setViewportView(tableEpisodes);
      }
    }
    {
      JButton btnRemoveEpisode = new JButton("");
      btnRemoveEpisode.setAction(new RemoveEpisodeAction());
      btnRemoveEpisode.setIcon(IconManager.LIST_REMOVE);
      episodesPanel.add(btnRemoveEpisode, "2, 4, default, top");
    }

    /**
     * Button pane
     */
    {
      JPanel bottomPane = new JPanel();
      getContentPane().add(bottomPane, BorderLayout.SOUTH);
      bottomPane.setLayout(
          new FormLayout(new ColumnSpec[] { ColumnSpec.decode("371px:grow"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, },
              new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JPanel buttonPane = new JPanel();
      bottomPane.add(buttonPane, "2, 2, left, top");
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPane.setLayout(layout);
      {
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        buttonPane.add(okButton);
        okButton.setAction(new OKAction());
        okButton.setActionCommand("OK");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        buttonPane.add(cancelButton);
        cancelButton.setAction(new CancelAction());
        cancelButton.setActionCommand("Cancel");
      }
      if (inQueue) {
        JButton btnAbort = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
        btnAbort.setAction(new AbortAction());
        buttonPane.add(btnAbort);
      }

    }

    initDataBindings();

    {
      lvlTvShowPath.setText(tvShow.getPath());
      tfTitle.setText(tvShow.getTitle());
      tfSorttitle.setText(tvShow.getSortTitle());
      tpPlot.setText(tvShow.getPlot());
      lblPoster.setImagePath(tvShow.getArtworkFilename(MediaFileType.POSTER));
      lblThumb.setImagePath(tvShowToEdit.getArtworkFilename(MediaFileType.THUMB));
      lblLogo.setImagePath(tvShowToEdit.getArtworkFilename(MediaFileType.LOGO));
      lblClearlogo.setImagePath(tvShowToEdit.getArtworkFilename(MediaFileType.CLEARLOGO));
      lblClearart.setImagePath(tvShowToEdit.getArtworkFilename(MediaFileType.CLEARART));
      tfStudio.setText(tvShow.getProductionCompany());

      int year = 0;
      try {
        year = Integer.parseInt(tvShow.getYear());
      }
      catch (Exception e) {
      }
      spYear.setValue(year);
      spDateAdded.setValue(tvShow.getDateAdded());

      for (TvShowActor origCast : tvShow.getActors()) {
        TvShowActor actor = new TvShowActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumbUrl(origCast.getThumbUrl());
        actors.add(actor);
      }

      for (MediaGenres genre : tvShow.getGenres()) {
        genres.add(genre);
      }

      // for (MediaTrailer trailer : tvShow.getTrailers()) {
      // trailers.add(trailer);
      // }

      for (String tag : tvShowToEdit.getTags()) {
        tags.add(tag);
      }

      List<TvShowEpisode> epl = new ArrayList<>(tvShowToEdit.getEpisodes());
      // custom sort per filename (just this time)
      // for unknown EPs (-1/-1) this is extremely useful to sort like on filesystem
      // and for already renamed ones, it makes no difference
      Collections.sort(epl, new Comparator<TvShowEpisode>() {
        public int compare(TvShowEpisode s1, TvShowEpisode s2) {
          return s1.getMediaFiles(MediaFileType.VIDEO).get(0).getFile().compareTo(s2.getMediaFiles(MediaFileType.VIDEO).get(0).getFile());
        }
      });

      for (TvShowEpisode episode : epl) {
        TvShowEpisodeEditorContainer container = new TvShowEpisodeEditorContainer();
        container.tvShowEpisode = episode;
        container.dvdOrder = episode.isDvdOrder();
        container.season = episode.getSeason();
        container.episode = episode.getEpisode();
        episodes.add(container);
      }

      if (((DefaultComboBoxModel) cbCertification.getModel()).getIndexOf(tvShow.getCertification()) == -1) {
        cbCertification.addItem(tvShow.getCertification());
      }

    }
    lblBanner = new ImageLabel();
    lblBanner.setAlternativeText(BUNDLE.getString("image.notfound.banner")); //$NON-NLS-1$
    lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    lblBanner.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.BANNER, tvShowList.getAvailableArtworkScrapers(),
            lblBanner, null, null, MediaType.TV_SHOW);
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
      }
    });
    details1Panel.add(lblBanner, "4, 24, 15, 3, fill, fill");
    lblBanner.setImagePath(tvShow.getArtworkFilename(MediaFileType.BANNER));
    {
      // JLabel lblFanart = new JLabel("");
      lblFanart = new ImageLabel();
      lblFanart.setAlternativeText(BUNDLE.getString("image.notfound.fanart")); //$NON-NLS-1$
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.FANART, tvShowList.getAvailableArtworkScrapers(),
              lblFanart, null, null, MediaType.TV_SHOW);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      details1Panel.add(lblFanart, "22, 22, 3, 5, fill, fill");
    }
    lblFanart.setImagePath(tvShow.getArtworkFilename(MediaFileType.FANART));

    // adjust columnn titles - we have to do it this way - thx to windowbuilder pro
    tableActors.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableActors.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.role")); //$NON-NLS-1$

    tableEpisodes.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
    tableEpisodes.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.filename")); //$NON-NLS-1$
    tableEpisodes.getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.season")); //$NON-NLS-1$
    tableEpisodes.getColumnModel().getColumn(3).setHeaderValue(BUNDLE.getString("metatag.episode")); //$NON-NLS-1$
    tableEpisodes.getColumnModel().getColumn(4).setHeaderValue(BUNDLE.getString("metatag.dvdorder")); //$NON-NLS-1$
    tableEpisodes.getColumnModel().getColumn(2).setMaxWidth(150);
    tableEpisodes.getColumnModel().getColumn(3).setMaxWidth(150);
    tableEpisodes.getColumnModel().getColumn(2).setCellEditor(new TableSpinnerEditor());
    tableEpisodes.getColumnModel().getColumn(3).setCellEditor(new TableSpinnerEditor());

    // adjust table columns
    TableColumnResizer.adjustColumnPreferredWidths(tableActors, 6);
    // TableColumnResizer.adjustColumnPreferredWidths(tableTrailer, 6);
    TableColumnResizer.adjustColumnPreferredWidths(tableEpisodes, 6);
  }

  private class OKAction extends AbstractAction {
    private static final long serialVersionUID = 6699599213348390696L;

    public OKAction() {
      putValue(NAME, BUNDLE.getString("Button.ok")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.APPLY);
      putValue(LARGE_ICON_KEY, IconManager.APPLY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      tvShowToEdit.setTitle(tfTitle.getText());
      tvShowToEdit.setSortTitle(tfSorttitle.getText());
      tvShowToEdit.setYear(String.valueOf(spYear.getValue()));
      tvShowToEdit.setPlot(tpPlot.getText());
      tvShowToEdit.setRuntime((Integer) spRuntime.getValue());
      // sync of media ids
      // first round -> add existing ids
      for (MediaId id : ids) {
        // only process non empty ids
        // changed; if empty/0/null value gets set, it is removed in setter ;)
        // if (StringUtils.isAnyBlank(id.key, id.value)) {
        // continue;
        // }
        // first try to cast it into an Integer
        try {
          Integer value = Integer.parseInt(id.value);
          // cool, it is an Integer
          tvShowToEdit.setId(id.key, value);
        }
        catch (NumberFormatException ex) {
          // okay, we set it as a String
          tvShowToEdit.setId(id.key, id.value);
        }
      }
      // second round -> remove deleted ids
      List<String> removeIds = new ArrayList<>();
      for (Entry<String, Object> entry : tvShowToEdit.getIds().entrySet()) {
        MediaId id = new MediaId(entry.getKey());
        if (!ids.contains(id)) {
          removeIds.add(entry.getKey());
        }
      }
      for (String id : removeIds) {
        tvShowToEdit.getIds().remove(id);
      }
      // tvShowToEdit.setImdbId(tfImdbId.getText());
      //
      // if (StringUtils.isNotBlank(tfTvdbId.getText())) {
      // try {
      // tvShowToEdit.setTvdbId(tfTvdbId.getText());
      // }
      // catch (NumberFormatException ex) {
      // JOptionPane.showMessageDialog(null, BUNDLE.getString("tmdb.wrongformat")); //$NON-NLS-1$
      // return;
      // }
      // }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        tvShowToEdit.setCertification((Certification) certification);
      }

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.POSTER))) {
        tvShowToEdit.setArtworkUrl(lblPoster.getImageUrl(), MediaFileType.POSTER);
        tvShowToEdit.downloadArtwork(MediaFileType.POSTER);
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.FANART))) {
        tvShowToEdit.setArtworkUrl(lblFanart.getImageUrl(), MediaFileType.FANART);
        tvShowToEdit.downloadArtwork(MediaFileType.FANART);
      }

      if (!StringUtils.isEmpty(lblBanner.getImageUrl()) && !lblBanner.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.BANNER))) {
        tvShowToEdit.setArtworkUrl(lblBanner.getImageUrl(), MediaFileType.BANNER);
        tvShowToEdit.downloadArtwork(MediaFileType.BANNER);
      }

      if (!StringUtils.isEmpty(lblLogo.getImageUrl()) && !lblLogo.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.LOGO))) {
        tvShowToEdit.setArtworkUrl(lblLogo.getImageUrl(), MediaFileType.LOGO);
        tvShowToEdit.downloadArtwork(MediaFileType.LOGO);
      }

      if (!StringUtils.isEmpty(lblClearlogo.getImageUrl())
          && !lblClearlogo.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.CLEARLOGO))) {
        tvShowToEdit.setArtworkUrl(lblClearlogo.getImageUrl(), MediaFileType.CLEARLOGO);
        tvShowToEdit.downloadArtwork(MediaFileType.CLEARLOGO);
      }

      if (!StringUtils.isEmpty(lblClearart.getImageUrl()) && !lblClearart.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.CLEARART))) {
        tvShowToEdit.setArtworkUrl(lblClearart.getImageUrl(), MediaFileType.CLEARART);
        tvShowToEdit.downloadArtwork(MediaFileType.CLEARART);
      }

      if (!StringUtils.isEmpty(lblThumb.getImageUrl()) && !lblThumb.getImageUrl().equals(tvShowToEdit.getArtworkUrl(MediaFileType.THUMB))) {
        tvShowToEdit.setArtworkUrl(lblThumb.getImageUrl(), MediaFileType.THUMB);
        tvShowToEdit.downloadArtwork(MediaFileType.THUMB);
      }

      tvShowToEdit.setProductionCompany(tfStudio.getText());
      tvShowToEdit.setActors(actors);
      tvShowToEdit.setGenres(genres);

      tvShowToEdit.setTags(tags);
      tvShowToEdit.setDateAdded((Date) spDateAdded.getValue());
      tvShowToEdit.setFirstAired(dpPremiered.getDate());

      tvShowToEdit.setStatus(cbStatus.getSelectedItem().toString());

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (tvShowToEdit.getRating() != rating) {
        tvShowToEdit.setRating(rating);
        tvShowToEdit.setVotes(1);
      }

      // adapt episodes according to the episode table (in a 2 way sync)
      // remove episodes
      for (int i = tvShowToEdit.getEpisodeCount() - 1; i >= 0; i--) {
        boolean found = false;
        TvShowEpisode episode = tvShowToEdit.getEpisodes().get(i);
        for (TvShowEpisodeEditorContainer container : episodes) {
          if (container.tvShowEpisode == episode) {
            found = true;
            break;
          }
        }

        if (!found) {
          tvShowToEdit.removeEpisode(episode);
        }
      }

      // add episodes
      for (TvShowEpisodeEditorContainer container : episodes) {
        boolean found = false;
        boolean shouldStore = false;

        if (container.dvdOrder != container.tvShowEpisode.isDvdOrder()) {
          container.tvShowEpisode.setDvdOrder(container.dvdOrder);
          shouldStore = true;
        }

        if (container.episode != container.tvShowEpisode.getEpisode()) {
          if (container.dvdOrder) {
            container.tvShowEpisode.setDvdEpisode(container.episode);
          }
          else {
            container.tvShowEpisode.setAiredEpisode(container.episode);
          }

          shouldStore = true;
        }

        if (container.season != container.tvShowEpisode.getSeason()) {
          if (container.dvdOrder) {
            container.tvShowEpisode.setDvdSeason(container.season);
          }
          else {
            container.tvShowEpisode.setAiredSeason(container.season);
          }
          shouldStore = true;
        }

        for (TvShowEpisode episode : tvShowToEdit.getEpisodes()) {
          if (container.tvShowEpisode == episode) {
            found = true;
            break;
          }
        }

        if (!found) {
          container.tvShowEpisode.writeNFO();
          container.tvShowEpisode.saveToDb();
          tvShowToEdit.addEpisode(container.tvShowEpisode);
        }
        else if (shouldStore) {
          container.tvShowEpisode.writeNFO();
          container.tvShowEpisode.saveToDb();
        }
      }

      tvShowToEdit.writeNFO();
      tvShowToEdit.saveToDb();

      if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
        TmmTask task = new SyncTraktTvTask(null, Arrays.asList(tvShowToEdit));
        TmmTaskManager.getInstance().addUnnamedTask(task);
      }

      setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    private static final long serialVersionUID = -4617793684152607277L;

    public CancelAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.CANCEL);
      putValue(LARGE_ICON_KEY, IconManager.CANCEL);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }

  private class AddActorAction extends AbstractAction {
    private static final long serialVersionUID = -5879601617842300526L;

    public AddActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TvShowActor actor = new TvShowActor(BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
      actors.add(0, actor);
    }
  }

  private class RemoveActorAction extends AbstractAction {
    private static final long serialVersionUID = 6970920169867315771L;

    public RemoveActorAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      if (row > -1) {
        row = tableActors.convertRowIndexToModel(row);
        actors.remove(row);
      }
    }
  }

  private class AddGenreAction extends AbstractAction {
    private static final long serialVersionUID = 6666302391216952247L;

    public AddGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = null;
      Object item = cbGenres.getSelectedItem();

      // check, if text is selected (from auto completion), in this case we just
      // remove the selection
      Component editorComponent = cbGenres.getEditor().getEditorComponent();
      if (editorComponent instanceof JTextField) {
        JTextField tf = (JTextField) editorComponent;
        String selectedText = tf.getSelectedText();
        if (selectedText != null) {
          tf.setSelectionStart(0);
          tf.setSelectionEnd(0);
          tf.setCaretPosition(tf.getText().length());
          return;
        }
      }

      // genre
      if (item instanceof MediaGenres) {
        newGenre = (MediaGenres) item;
      }

      // newly created genre?
      if (item instanceof String) {
        newGenre = MediaGenres.getGenre((String) item);
      }

      // add genre if it is not already in the list
      if (newGenre != null && !genres.contains(newGenre)) {
        genres.add(newGenre);

        // set text combobox text input to ""
        if (editorComponent instanceof JTextField) {
          cbGenresAutoCompleteSupport.setFirstItem(null);
          cbGenres.setSelectedIndex(0);
          cbGenresAutoCompleteSupport.removeFirstItem();
        }
      }
    }
  }

  private class RemoveGenreAction extends AbstractAction {
    private static final long serialVersionUID = -5459615776560234688L;

    public RemoveGenreAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<MediaGenres> selectedGenres = (List<MediaGenres>) listGenres.getSelectedValuesList();
      for (MediaGenres genre : selectedGenres) {
        genres.remove(genre);
      }
    }
  }

  /*
   * private class AddTrailerAction extends AbstractAction { private static final long serialVersionUID = 5448745104881472479L;
   * 
   * public AddTrailerAction() { putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.add")); //$NON-NLS-1$ }
   * 
   * @Override public void actionPerformed(ActionEvent e) { MediaTrailer trailer = new MediaTrailer(); trailer.setName("unknown");
   * trailer.setProvider("unknown"); trailer.setQuality("unknown"); trailer.setUrl("http://"); trailers.add(0, trailer); } }
   * 
   * private class RemoveTrailerAction extends AbstractAction { private static final long serialVersionUID = -6956921050689930101L;
   * 
   * public RemoveTrailerAction() { putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.remove")); //$NON-NLS-1$ }
   * 
   * @Override public void actionPerformed(ActionEvent e) { int row = tableTrailer.getSelectedRow(); if (row > -1) { row =
   * tableTrailer.convertRowIndexToModel(row); trailers.remove(row); } } }
   */

  /**
   * Shows the dialog and returns whether the work on the queue should be continued.
   * 
   * @return true, if successful
   */
  public boolean showDialog() {
    setVisible(true);
    return continueQueue;
  }

  private class AddTagAction extends AbstractAction {
    private static final long serialVersionUID = 9160043031922897785L;

    public AddTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();
      if (StringUtils.isBlank(newTag)) {
        return;
      }

      // check, if text is selected (from auto completion), in this case we just
      // remove the selection
      Component editorComponent = cbTags.getEditor().getEditorComponent();
      if (editorComponent instanceof JTextField) {
        JTextField tf = (JTextField) editorComponent;
        String selectedText = tf.getSelectedText();
        if (selectedText != null) {
          tf.setSelectionStart(0);
          tf.setSelectionEnd(0);
          tf.setCaretPosition(tf.getText().length());
          return;
        }
      }

      // search if this tag already has been added
      boolean tagFound = false;
      for (String tag : tags) {
        if (tag.equals(newTag)) {
          tagFound = true;
          break;
        }
      }

      // add tag
      if (!tagFound) {
        tags.add(newTag);

        // set text combobox text input to ""
        if (editorComponent instanceof JTextField) {
          cbTagsAutoCompleteSupport.setFirstItem("");
          cbTags.setSelectedIndex(0);
          cbTagsAutoCompleteSupport.removeFirstItem();
        }
      }
    }
  }

  private class AddIdAction extends AbstractAction {
    private static final long serialVersionUID = 2903255414553349267L;

    public AddIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.add")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      MediaId Id = new MediaId(); // $NON-NLS-1$
      ids.add(Id);
    }
  }

  private class RemoveIdAction extends AbstractAction {
    private static final long serialVersionUID = -7079826950827356996L;

    public RemoveIdAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("id.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableIds.getSelectedRow();
      if (row > -1) {
        row = tableIds.convertRowIndexToModel(row);
        ids.remove(row);
      }
    }
  }

  private class RemoveTagAction extends AbstractAction {
    private static final long serialVersionUID = -1580945350962234235L;

    public RemoveTagAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<String> selectedTags = listTags.getSelectedValuesList();
      for (String tag : selectedTags) {
        tags.remove(tag);
      }
    }
  }

  private class AbortAction extends AbstractAction {
    private static final long serialVersionUID = -7652218354710642510L;

    public AbortAction() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.PROCESS_STOP);
      putValue(LARGE_ICON_KEY, IconManager.PROCESS_STOP);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
    }
  }

  public class TvShowEpisodeEditorContainer {
    TvShowEpisode tvShowEpisode;
    int           season;
    int           episode;
    boolean       dvdOrder = false;

    public String getEpisodeTitle() {
      return tvShowEpisode.getTitle();
    }

    public String getMediaFilename() {
      List<MediaFile> mfs = tvShowEpisode.getMediaFiles(MediaFileType.VIDEO);
      if (mfs != null && mfs.size() > 0) {
        return mfs.get(0).getFile().getAbsolutePath();
      }
      else {
        return "";
      }
    }

    public int getEpisode() {
      return episode;
    }

    public void setEpisode(int episode) {
      this.episode = episode;
    }

    public int getSeason() {
      return season;
    }

    public void setSeason(int season) {
      this.season = season;
    }

    public boolean isDvdOrder() {
      return this.dvdOrder;
    }

    public void setDvdOrder(boolean dvdOrder) {
      this.dvdOrder = dvdOrder;
    }
  }

  protected void initDataBindings() {
    jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, actors, tableActors);
    //
    BeanProperty<TvShowActor, String> castBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(castBeanProperty);
    //
    BeanProperty<TvShowActor, String> castBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(castBeanProperty_1);
    //
    jTableBinding.bind();
    //
    jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding_1.bind();
    //
    jTableBinding_2 = SwingBindings.createJTableBinding(UpdateStrategy.READ, episodes, tableEpisodes);
    //
    BeanProperty<TvShowEpisodeEditorContainer, String> tvShowEpisodeEditorContainerBeanProperty = BeanProperty.create("episodeTitle");
    jTableBinding_2.addColumnBinding(tvShowEpisodeEditorContainerBeanProperty);
    //
    BeanProperty<TvShowEpisodeEditorContainer, String> tvShowEpisodeEditorContainerBeanProperty_1 = BeanProperty.create("mediaFilename");
    jTableBinding_2.addColumnBinding(tvShowEpisodeEditorContainerBeanProperty_1);
    //
    BeanProperty<TvShowEpisodeEditorContainer, Integer> tvShowEpisodeEditorContainerBeanProperty_2 = BeanProperty.create("season");
    jTableBinding_2.addColumnBinding(tvShowEpisodeEditorContainerBeanProperty_2);
    //
    BeanProperty<TvShowEpisodeEditorContainer, Integer> tvShowEpisodeEditorContainerBeanProperty_3 = BeanProperty.create("episode");
    jTableBinding_2.addColumnBinding(tvShowEpisodeEditorContainerBeanProperty_3);
    //
    BeanProperty<TvShowEpisodeEditorContainer, Boolean> tvShowEpisodeEditorContainerBeanProperty_4 = BeanProperty.create("dvdOrder");
    jTableBinding_2.addColumnBinding(tvShowEpisodeEditorContainerBeanProperty_4).setColumnClass(Boolean.class);
    //
    jTableBinding_2.bind();
  }

  @Override
  public void dispose() {
    super.dispose();
    jTableBinding.unbind();
    jListBinding.unbind();
    jListBinding_1.unbind();
    jTableBinding_2.unbind();
    dpPremiered.cleanup();
  }

  @Override
  public void pack() {
    // do not let it pack - it looks weird
  }

  private class CloneEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -3255090541823134232L;

    public CloneEpisodeAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshowepisode.clone")); //$NON-NLS-1$
      putValue(SMALL_ICON, IconManager.COPY);
      putValue(LARGE_ICON_KEY, IconManager.COPY);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      int row = tableEpisodes.getSelectedRow();
      if (row > -1) {
        row = tableEpisodes.convertRowIndexToModel(row);
        TvShowEpisodeEditorContainer origContainer = episodes.get(row);
        TvShowEpisodeEditorContainer newContainer = new TvShowEpisodeEditorContainer();
        newContainer.tvShowEpisode = new TvShowEpisode(origContainer.tvShowEpisode);
        newContainer.tvShowEpisode.setTitle(origContainer.tvShowEpisode.getTitle() + " (clone)");
        newContainer.episode = -1;
        newContainer.season = newContainer.tvShowEpisode.getSeason();
        episodes.add(row + 1, newContainer);
      }
    }
  }

  private class RemoveEpisodeAction extends AbstractAction {
    private static final long serialVersionUID = -8233854057648972649L;

    public RemoveEpisodeAction() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshowepisode.remove")); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int row = tableEpisodes.getSelectedRow();
      if (row > -1) {
        row = tableEpisodes.convertRowIndexToModel(row);
        episodes.remove(row);
      }
    }
  }
}
