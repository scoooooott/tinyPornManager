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
package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShow;
import org.tinymediamanager.core.tvshow.TvShowActor;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.ui.EqualsLayout;
import org.tinymediamanager.ui.MainWindow;
import org.tinymediamanager.ui.TableColumnAdjuster;
import org.tinymediamanager.ui.TmmWindowSaver;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.AutocompleteComboBox;
import org.tinymediamanager.ui.components.ImageLabel;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog;
import org.tinymediamanager.ui.dialogs.ImageChooserDialog.ImageType;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class TvShowEditor.
 * 
 * @author Manuel Laggner
 */
public class TvShowEditorDialog extends JDialog {

  /** The Constant serialVersionUID. */
  private static final long           serialVersionUID  = 3270218410302989845L;

  /** The Constant BUNDLE. */
  private final static ResourceBundle BUNDLE            = ResourceBundle.getBundle("messages", new UTF8Control());            //$NON-NLS-1$

  /** The details1 panel. */
  private final JPanel                details1Panel     = new JPanel();

  /** The details2 panel. */
  private final JPanel                details2Panel     = new JPanel();

  /** The tv show to edit. */
  private TvShow                      tvShowToEdit;

  /** The tv show list. */
  private TvShowList                  tvShowList        = TvShowList.getInstance();

  /** The tf title. */
  private JTextField                  tfTitle;

  /** The tf year. */
  private JSpinner                    spYear;

  /** The tp plot. */
  private JTextPane                   tpPlot;

  /** The table. */
  private JTable                      tableActors;

  /** The lvl tv show path. */
  private JLabel                      lvlTvShowPath;

  /** The lbl poster. */
  private ImageLabel                  lblPoster;

  /** The lbl fanart. */
  private ImageLabel                  lblFanart;

  /** The lbl banner. */
  private ImageLabel                  lblBanner;

  /** The actors. */
  private List<TvShowActor>           actors            = ObservableCollections.observableList(new ArrayList<TvShowActor>());

  /** The genres. */
  private List<MediaGenres>           genres            = ObservableCollections.observableList(new ArrayList<MediaGenres>());

  /** The trailers. */
  private List<MediaTrailer>          trailers          = ObservableCollections.observableList(new ArrayList<MediaTrailer>());

  /** The tags. */
  private List<String>                tags              = ObservableCollections.observableList(new ArrayList<String>());

  /** The action ok. */
  private final Action                actionOK          = new SwingAction();

  /** The action cancel. */
  private final Action                actionCancel      = new SwingAction_1();

  /** The action add actor. */
  private final Action                actionAddActor    = new SwingAction_4();

  /** The action remove actor. */
  private final Action                actionRemoveActor = new SwingAction_5();

  /** The sp runtime. */
  private JSpinner                    spRuntime;

  /** The tf production companies. */
  private JTextField                  tfProductionCompanies;

  /** The list genres. */
  private JList                       listGenres;

  /** The action add genre. */
  private final Action                actionAddGenre    = new SwingAction_2();

  /** The action remove genre. */
  private final Action                actionRemoveGenre = new SwingAction_3();

  /** The cb genres. */
  private JComboBox                   cbGenres;

  /** The sp rating. */
  private JSpinner                    spRating;

  /** The cb certification. */
  private JComboBox                   cbCertification;

  /** The tf imdb id. */
  private JTextField                  tfImdbId;

  /** The tf tmdb id. */
  private JTextField                  tfTvdbId;

  /** The lbl imdb id. */
  private JLabel                      lblImdbId;

  /** The lbl tmdb id. */
  private JLabel                      lblTvdbId;

  /** The table trailer. */
  private JTable                      tableTrailer;

  /** The action. */
  private final Action                action            = new SwingAction_6();

  /** The action_1. */
  private final Action                action_1          = new SwingAction_7();

  /** The cb tags. */
  private JComboBox                   cbTags;

  /** The list tags. */
  private JList                       listTags;

  /** The action_2. */
  private final Action                action_2          = new SwingAction_8();

  /** The action_3. */
  private final Action                action_3          = new SwingAction_9();

  /** The sp date added. */
  private JSpinner                    spDateAdded;

  /** The continue queue. */
  private boolean                     continueQueue     = true;

  /** The abort action. */
  private final Action                abortAction       = new SwingAction_10();

  /**
   * Instantiates a new tv show editor dialog.
   * 
   * @param tvShow
   *          the tv show
   * @param inQueue
   *          the in queue
   */
  public TvShowEditorDialog(TvShow tvShow, boolean inQueue) {
    setModal(true);
    setIconImage(Globals.logo);
    setTitle(BUNDLE.getString("tvshow.edit")); //$NON-NLS-1$
    setName("tvShowEditor");
    setBounds(5, 5, 950, 700);
    TmmWindowSaver.loadSettings(this);

    tvShowToEdit = tvShow;
    getContentPane().setLayout(new BorderLayout());
    {
      JPanel panelPath = new JPanel();
      getContentPane().add(panelPath, BorderLayout.NORTH);
      panelPath.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
          FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC,
          RowSpec.decode("15px"), FormFactory.RELATED_GAP_ROWSPEC, }));

      JLabel lblTvShowPathT = new JLabel(BUNDLE.getString("metatag.path")); //$NON-NLS-1$
      panelPath.add(lblTvShowPathT, "2, 2, left, top");

      lvlTvShowPath = new JLabel("");
      lvlTvShowPath.setFont(new Font("Dialog", Font.BOLD, 14));
      panelPath.add(lvlTvShowPath, "5, 2, left, top");
    }

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.NORTH);
    tabbedPane.addTab(BUNDLE.getString("metatag.details"), details1Panel); //$NON-NLS-1$
    getContentPane().add(tabbedPane, BorderLayout.CENTER);

    details1Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details1Panel
        .setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px"), FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
            FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("250px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("top:max(75px;default)"),
            FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("75px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
            FormFactory.NARROW_LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC,
            RowSpec.decode("fill:30px:grow(2)"), }));

    {
      JLabel lblTitle = new JLabel(BUNDLE.getString("metatag.title")); //$NON-NLS-1$
      details1Panel.add(lblTitle, "2, 2, right, default");
    }
    {
      tfTitle = new JTextField();
      details1Panel.add(tfTitle, "4, 2, 9, 1, fill, default");
      tfTitle.setColumns(10);
    }
    {
      lblPoster = new ImageLabel();
      lblPoster.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.POSTER, tvShowList.getArtworkProviders(), lblPoster,
              null, null);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      lblPoster.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      details1Panel.add(lblPoster, "14, 2, 3, 13, fill, fill");
    }
    {
      JLabel lblYear = new JLabel(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
      details1Panel.add(lblYear, "2, 4, right, default");
    }
    {
      spYear = new JSpinner();
      details1Panel.add(spYear, "4, 4, fill, top");
    }
    {
      JLabel lblRuntime = new JLabel(BUNDLE.getString("metatag.runtime")); //$NON-NLS-1$
      details1Panel.add(lblRuntime, "8, 4, right, default");
    }
    {
      spRuntime = new JSpinner();
      details1Panel.add(spRuntime, "10, 4, fill, default");
    }
    {
      JLabel lblMin = new JLabel(BUNDLE.getString("metatag.minutes")); //$NON-NLS-1$
      details1Panel.add(lblMin, "12, 4");
    }
    {
      JLabel lblRating = new JLabel(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
      details1Panel.add(lblRating, "2, 6, right, default");
    }
    {
      spRating = new JSpinner();
      details1Panel.add(spRating, "4, 6");
    }
    {
      JLabel lblCertification = new JLabel(BUNDLE.getString("metatag.certification")); //$NON-NLS-1$
      details1Panel.add(lblCertification, "8, 6, right, default");
    }
    {
      cbCertification = new JComboBox();
      for (Certification cert : Certification.getCertificationsforCountry(Globals.settings.getTvShowSettings().getCertificationCountry())) {
        cbCertification.addItem(cert);
      }
      details1Panel.add(cbCertification, "10, 6, 3, 1, fill, default");
    }
    {
      lblImdbId = new JLabel(BUNDLE.getString("metatag.imdb")); //$NON-NLS-1$
      details1Panel.add(lblImdbId, "2, 8, right, default");
    }
    {
      tfImdbId = new JTextField();
      lblImdbId.setLabelFor(tfImdbId);
      details1Panel.add(tfImdbId, "4, 8, 3, 1, fill, default");
      tfImdbId.setColumns(10);
    }
    {
      lblTvdbId = new JLabel(BUNDLE.getString("metatag.tvdb")); //$NON-NLS-1$
      details1Panel.add(lblTvdbId, "8, 8, right, default");
    }
    {
      tfTvdbId = new JTextField();
      lblTvdbId.setLabelFor(tfTvdbId);
      details1Panel.add(tfTvdbId, "10, 8, 3, 1, fill, default");
      tfTvdbId.setColumns(10);
    }
    {
      JLabel lblDateAdded = new JLabel(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
      details1Panel.add(lblDateAdded, "2, 10, right, default");
    }
    {
      spDateAdded = new JSpinner(new SpinnerDateModel());
      details1Panel.add(spDateAdded, "4, 10, 3, 1");
    }
    spDateAdded.setValue(tvShow.getDateAdded());
    {
      JLabel lblPlot = new JLabel(BUNDLE.getString("metatag.plot")); //$NON-NLS-1$
      details1Panel.add(lblPlot, "2, 12, right, top");
    }
    {
      JScrollPane scrollPanePlot = new JScrollPane();
      details1Panel.add(scrollPanePlot, "4, 12, 9, 3, fill, fill");
      {
        tpPlot = new JTextPane();
        scrollPanePlot.setViewportView(tpPlot);
      }
    }
    {
      JLabel lblStudio = new JLabel(BUNDLE.getString("metatag.studio")); //$NON-NLS-1$
      details1Panel.add(lblStudio, "2, 16, right, top");
    }
    {
      tfProductionCompanies = new JTextField();
      details1Panel.add(tfProductionCompanies, "4, 16, 9, 1");
      tfProductionCompanies.setText(tvShow.getProductionCompany());
    }

    /**
     * DetailsPanel 2
     */
    tabbedPane.addTab(BUNDLE.getString("metatag.details2"), details2Panel); //$NON-NLS-1$
    details2Panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    details2Panel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(40dlu;default)"),
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
        FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("100px:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:30px:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow(2)"), }));
    {
      JLabel lblActors = new JLabel(BUNDLE.getString("metatag.actors")); //$NON-NLS-1$
      details2Panel.add(lblActors, "2, 2, right, default");
    }
    {
      JScrollPane scrollPane = new JScrollPane();
      details2Panel.add(scrollPane, "4, 2, 1, 7");
      {
        tableActors = new JTable();
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
      btnAddActor.setAction(actionAddActor);
      btnAddActor.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      details2Panel.add(btnAddActor, "2, 4, right, top");
    }
    {
      JScrollPane scrollPaneGenres = new JScrollPane();
      details2Panel.add(scrollPaneGenres, "8, 2, 1, 5");
      {
        listGenres = new JList();
        scrollPaneGenres.setViewportView(listGenres);
      }
    }
    {
      JButton btnAddGenre = new JButton("");
      btnAddGenre.setAction(actionAddGenre);
      btnAddGenre.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddGenre.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddGenre, "6, 4, right, top");
    }
    {
      JButton btnRemoveActor = new JButton(BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
      btnRemoveActor.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveActor.setAction(actionRemoveActor);
      btnRemoveActor.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      details2Panel.add(btnRemoveActor, "2,6, right, top");
    }

    {
      JButton btnRemoveGenre = new JButton("");
      btnRemoveGenre.setAction(actionRemoveGenre);
      btnRemoveGenre.setMargin(new Insets(2, 2, 2, 2));
      btnRemoveGenre.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      details2Panel.add(btnRemoveGenre, "6, 6, right, top");
    }
    {
      // cbGenres = new JComboBox(MediaGenres2.values());
      cbGenres = new AutocompleteComboBox(MediaGenres.values());
      cbGenres.setEditable(true);
      details2Panel.add(cbGenres, "8,8");
    }

    {
      JLabel lblTrailer = new JLabel(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
      details2Panel.add(lblTrailer, "2, 10, right, default");
    }
    {
      JScrollPane scrollPaneTrailer = new JScrollPane();
      details2Panel.add(scrollPaneTrailer, "4, 10, 5, 5");
      tableTrailer = new JTable();
      scrollPaneTrailer.setViewportView(tableTrailer);
    }
    {
      JButton btnAddTrailer = new JButton("");
      btnAddTrailer.setAction(action);
      btnAddTrailer.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTrailer, "2, 12, right, top");
    }
    {
      JButton btnRemoveTrailer = new JButton("");
      btnRemoveTrailer.setAction(action_1);
      btnRemoveTrailer.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveTrailer.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTrailer, "2, 14, right, top");
    }
    {
      JLabel lblTags = new JLabel(BUNDLE.getString("metatag.tags")); //$NON-NLS-1$
      details2Panel.add(lblTags, "2, 16, right, default");
    }
    {
      JScrollPane scrollPaneTags = new JScrollPane();
      details2Panel.add(scrollPaneTags, "4, 16, 1, 5");
      listTags = new JList();
      scrollPaneTags.setViewportView(listTags);
    }
    {
      JButton btnAddTag = new JButton("");
      btnAddTag.setAction(action_2);
      btnAddTag.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Add.png")));
      btnAddTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnAddTag, "2, 18, right, top");
    }
    {
      JButton btnRemoveTag = new JButton("");
      btnRemoveTag.setAction(action_3);
      btnRemoveTag.setIcon(new ImageIcon(TvShowEditorDialog.class.getResource("/org/tinymediamanager/ui/images/Remove.png")));
      btnRemoveTag.setMargin(new Insets(2, 2, 2, 2));
      details2Panel.add(btnRemoveTag, "2, 20, right, top");
    }
    {
      cbTags = new AutocompleteComboBox(tvShowList.getTagsInTvShows().toArray());
      cbTags.setEditable(true);
      details2Panel.add(cbTags, "4, 22");
    }

    /**
     * Button pane
     */
    {
      JPanel bottomPane = new JPanel();
      getContentPane().add(bottomPane, BorderLayout.SOUTH);
      bottomPane.setLayout(new FormLayout(new ColumnSpec[] { ColumnSpec.decode("371px:grow"), FormFactory.DEFAULT_COLSPEC,
          FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("25px"),
          FormFactory.NARROW_LINE_GAP_ROWSPEC, }));

      JPanel buttonPane = new JPanel();
      bottomPane.add(buttonPane, "2, 2, left, top");
      EqualsLayout layout = new EqualsLayout(5);
      layout.setMinWidth(100);
      buttonPane.setLayout(layout);
      {
        JButton okButton = new JButton(BUNDLE.getString("Button.ok")); //$NON-NLS-1$
        buttonPane.add(okButton);
        okButton.setAction(actionOK);
        okButton.setActionCommand("OK");
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton(BUNDLE.getString("Button.cancel")); //$NON-NLS-1$
        buttonPane.add(cancelButton);
        cancelButton.setAction(actionCancel);
        cancelButton.setActionCommand("Cancel");
      }
      if (inQueue) {
        JButton btnAbort = new JButton(BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
        btnAbort.setAction(abortAction);
        buttonPane.add(btnAbort);
      }

    }

    initDataBindings();

    {
      lvlTvShowPath.setText(tvShow.getPath());
      tfTitle.setText(tvShow.getTitle());
      tfImdbId.setText(tvShow.getImdbId());
      tfTvdbId.setText(String.valueOf(tvShow.getId("tvdb")));
      tpPlot.setText(tvShow.getPlot());
      lblPoster.setImagePath(tvShow.getPoster());
      spRuntime.setValue(Integer.valueOf(tvShow.getRuntime()));

      int year = 0;
      try {
        year = Integer.valueOf(tvShow.getYear());
      }
      catch (Exception e) {
      }
      spYear.setValue(year);

      spYear.setEditor(new JSpinner.NumberEditor(spYear, "#"));
      spRating.setModel(new SpinnerNumberModel(tvShow.getRating(), 0.0, 10.0, 0.1));

      for (TvShowActor origCast : tvShow.getActors()) {
        TvShowActor actor = new TvShowActor();
        actor.setName(origCast.getName());
        actor.setCharacter(origCast.getCharacter());
        actor.setThumb(origCast.getThumb());
        actors.add(actor);
      }

      for (MediaGenres genre : tvShow.getGenres()) {
        genres.add(genre);
      }

      for (MediaTrailer trailer : tvShow.getTrailers()) {
        trailers.add(trailer);
      }

      for (String tag : tvShowToEdit.getTags()) {
        tags.add(tag);
      }

      cbCertification.setSelectedItem(tvShow.getCertification());

    }
    lblBanner = new ImageLabel();
    lblBanner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    lblBanner.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.BANNER, tvShowList.getArtworkProviders(), lblBanner,
            null, null);
        dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
        dialog.setVisible(true);
      }
    });
    details1Panel.add(lblBanner, "4, 18, 9, 3, fill, fill");
    lblBanner.setImagePath(tvShow.getBanner());
    {
      // JLabel lblFanart = new JLabel("");
      lblFanart = new ImageLabel();
      lblFanart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblFanart.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          ImageChooserDialog dialog = new ImageChooserDialog(tvShowToEdit.getIds(), ImageType.FANART, tvShowList.getArtworkProviders(), lblFanart,
              null, null);
          dialog.setLocationRelativeTo(MainWindow.getActiveInstance());
          dialog.setVisible(true);
        }
      });
      details1Panel.add(lblFanart, "14, 16, 3, 5, fill, fill");
    }
    lblFanart.setImagePath(tvShow.getFanart());

    // adjust table columns
    TableColumnAdjuster tableColumnAdjuster = new TableColumnAdjuster(tableTrailer);
    tableColumnAdjuster.setColumnDataIncluded(true);
    tableColumnAdjuster.setColumnHeaderIncluded(true);
    tableColumnAdjuster.adjustColumns();

    // adjust columnn titles - we have to do it this way - thx to windowbuilder pro
    tableActors.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableActors.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.role")); //$NON-NLS-1$

    tableTrailer.getColumnModel().getColumn(0).setHeaderValue(BUNDLE.getString("metatag.nfo")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(1).setHeaderValue(BUNDLE.getString("metatag.name")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(2).setHeaderValue(BUNDLE.getString("metatag.source")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(3).setHeaderValue(BUNDLE.getString("metatag.quality")); //$NON-NLS-1$
    tableTrailer.getColumnModel().getColumn(4).setHeaderValue(BUNDLE.getString("metatag.url")); //$NON-NLS-1$

    // // implement listener to simulate button group
    // tableTrailer.getModel().addTableModelListener(new TableModelListener() {
    // @Override
    // public void tableChanged(TableModelEvent arg0) {
    // // click on the checkbox
    // if (arg0.getColumn() == 0) {
    // int row = arg0.getFirstRow();
    // MediaTrailer changedTrailer = trailers.get(row);
    // // if flag inNFO was changed, change all other trailers flags
    // if (changedTrailer.getInNfo()) {
    // for (MediaTrailer trailer : trailers) {
    // if (trailer != changedTrailer) {
    // trailer.setInNfo(Boolean.FALSE);
    // }
    // }
    // }
    // }
    // }
    // });
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.change")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      tvShowToEdit.setTitle(tfTitle.getText());
      tvShowToEdit.setYear(String.valueOf(spYear.getValue()));
      tvShowToEdit.setRuntime((Integer) spRuntime.getValue());
      tvShowToEdit.setImdbId(tfImdbId.getText());
      try {
        tvShowToEdit.setId("tvdb", Integer.parseInt(tfTvdbId.getText()));
      }
      catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(null, BUNDLE.getString("tmdb.wrongformat")); //$NON-NLS-1$
        return;
      }

      Object certification = cbCertification.getSelectedItem();
      if (certification instanceof Certification) {
        tvShowToEdit.setCertification((Certification) certification);
      }

      if (!StringUtils.isEmpty(lblPoster.getImageUrl()) && !lblPoster.getImageUrl().equals(tvShowToEdit.getPosterUrl())) {
        tvShowToEdit.setPosterUrl(lblPoster.getImageUrl());
        tvShowToEdit.writePosterImage();
      }

      if (!StringUtils.isEmpty(lblFanart.getImageUrl()) && !lblFanart.getImageUrl().equals(tvShowToEdit.getFanartUrl())) {
        tvShowToEdit.setFanartUrl(lblFanart.getImageUrl());
        tvShowToEdit.writeFanartImage();
      }

      if (!StringUtils.isEmpty(lblBanner.getImageUrl()) && !lblBanner.getImageUrl().equals(tvShowToEdit.getBannerUrl())) {
        tvShowToEdit.setBannerUrl(lblBanner.getImageUrl());
        tvShowToEdit.writeBannerImage();
      }

      tvShowToEdit.setProductionCompany(tfProductionCompanies.getText());
      tvShowToEdit.setActors(actors);
      tvShowToEdit.setGenres(genres);

      tvShowToEdit.removeAllTrailers();
      for (MediaTrailer trailer : trailers) {
        tvShowToEdit.addTrailer(trailer);
      }

      tvShowToEdit.setTags(tags);
      tvShowToEdit.setDateAdded((Date) spDateAdded.getValue());

      double tempRating = (Double) spRating.getValue();
      float rating = (float) tempRating;
      if (tvShowToEdit.getRating() != rating) {
        tvShowToEdit.setRating(rating);
        tvShowToEdit.setVotes(1);
      }

      tvShowToEdit.saveToDb();
      tvShowToEdit.writeNFO();
      setVisible(false);
      dispose();
    }
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
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
      dispose();
    }
  }

  /**
   * The Class SwingAction_4.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_4 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_4.
     */
    public SwingAction_4() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.add")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      TvShowActor actor = new TvShowActor(BUNDLE.getString("cast.actor.unknown"), BUNDLE.getString("cast.role.unknown")); //$NON-NLS-1$
      actors.add(0, actor);
    }
  }

  /**
   * The Class SwingAction_5.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_5 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_5.
     */
    public SwingAction_5() {
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("cast.actor.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableActors.getSelectedRow();
      row = tableActors.convertRowIndexToModel(row);
      actors.remove(row);
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
      // putValue(NAME, "SwingAction_2");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.add")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = null;
      Object item = cbGenres.getSelectedItem();

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
      }
    }
  }

  /**
   * The Class SwingAction_3.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_3 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_3.
     */
    public SwingAction_3() {
      // putValue(NAME, "SwingAction_3");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("genre.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaGenres newGenre = (MediaGenres) listGenres.getSelectedValue();
      // remove genre
      if (newGenre != null) {
        genres.remove(newGenre);
      }
    }
  }

  /**
   * The Class SwingAction_6.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_6 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new swing action_6.
     */
    public SwingAction_6() {
      // putValue(NAME, "SwingAction_6");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.add")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      MediaTrailer trailer = new MediaTrailer();
      trailer.setName("unknown");
      trailer.setProvider("unknown");
      trailer.setQuality("unknown");
      trailer.setUrl("http://");
      trailers.add(0, trailer);
    }
  }

  /**
   * The Class SwingAction_7.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_7 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6956921050689930101L;

    /**
     * Instantiates a new swing action_7.
     */
    public SwingAction_7() {
      // putValue(NAME, "SwingAction_7");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("trailer.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      int row = tableTrailer.getSelectedRow();
      row = tableTrailer.convertRowIndexToModel(row);
      trailers.remove(row);
    }
  }

  /**
   * Inits the data bindings.
   */
  protected void initDataBindings() {
    JTableBinding<TvShowActor, List<TvShowActor>, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, actors, tableActors);
    //
    BeanProperty<TvShowActor, String> castBeanProperty = BeanProperty.create("name");
    jTableBinding.addColumnBinding(castBeanProperty);
    //
    BeanProperty<TvShowActor, String> castBeanProperty_1 = BeanProperty.create("character");
    jTableBinding.addColumnBinding(castBeanProperty_1);
    //
    jTableBinding.bind();
    //
    JListBinding<MediaGenres, List<MediaGenres>, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, genres, listGenres);
    jListBinding.bind();
    //
    JTableBinding<MediaTrailer, List<MediaTrailer>, JTable> jTableBinding_1 = SwingBindings.createJTableBinding(UpdateStrategy.READ, trailers,
        tableTrailer);
    //
    BeanProperty<MediaTrailer, Boolean> trailerBeanProperty = BeanProperty.create("inNfo");
    jTableBinding_1.addColumnBinding(trailerBeanProperty).setColumnClass(Boolean.class);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_1 = BeanProperty.create("name");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_1);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_2 = BeanProperty.create("provider");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_2);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_3 = BeanProperty.create("quality");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_3);
    //
    BeanProperty<MediaTrailer, String> trailerBeanProperty_4 = BeanProperty.create("url");
    jTableBinding_1.addColumnBinding(trailerBeanProperty_4);
    //
    jTableBinding_1.bind();
    //
    BeanProperty<TvShowList, List<String>> tvShowListBeanProperty = BeanProperty.create("tagsInTvShows");
    JComboBoxBinding<String, TvShowList, JComboBox> jComboBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, tvShowList,
        tvShowListBeanProperty, cbTags);
    jComboBinding.bind();
    //
    JListBinding<String, List<String>, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ, tags, listTags);
    jListBinding_1.bind();
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

  /**
   * The Class SwingAction_8.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_8 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 9160043031922897785L;

    /**
     * Instantiates a new swing action_8.
     */
    public SwingAction_8() {
      // putValue(NAME, "SwingAction_8");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.add")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      String newTag = (String) cbTags.getSelectedItem();
      boolean tagFound = false;

      // search if this tag already has been added
      for (String tag : tags) {
        if (tag.equals(newTag)) {
          tagFound = true;
          break;
        }
      }

      // add tag
      if (!tagFound) {
        tags.add(newTag);
      }
    }
  }

  /**
   * The Class SwingAction_9.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_9 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -1580945350962234235L;

    /**
     * Instantiates a new swing action_9.
     */
    public SwingAction_9() {
      // putValue(NAME, "SwingAction_9");
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tag.remove")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      String tag = (String) listTags.getSelectedValue();
      tags.remove(tag);
    }
  }

  /**
   * The Class SwingAction_10.
   * 
   * @author Manuel Laggner
   */
  private class SwingAction_10 extends AbstractAction {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7652218354710642510L;

    /**
     * Instantiates a new swing action_10.
     */
    public SwingAction_10() {
      putValue(NAME, BUNDLE.getString("Button.abortqueue")); //$NON-NLS-1$
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.edit.abortqueue.desc")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
      continueQueue = false;
      setVisible(false);
      dispose();
    }
  }
}
