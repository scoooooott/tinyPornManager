package org.tinymediamanager.ui.movies.settings;

import java.awt.Font;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieSettings;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.UTF8Control;

import net.miginfocom.swing.MigLayout;

public class MovieSettingsPanel extends JPanel {
  private static final long           serialVersionUID = -4173835431245178069L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private final MovieSettings         settings         = MovieModuleManager.MOVIE_SETTINGS;

  private JButton                     btnClearTraktData;
  private JCheckBox                   chckbxTraktSync;
  private JCheckBox                   chckbxRenameAfterScrape;
  private JCheckBox                   chckbxPersistUiFilters;
  private JCheckBox                   chckbxBuildImageCache;
  private JCheckBox                   chckbxRuntimeFromMi;

  public MovieSettingsPanel() {
    // UI initializations
    initComponents();
    initDataBindings();

    // logic initializations
    if (!Globals.isDonator()) {
      chckbxTraktSync.setSelected(false);
      chckbxTraktSync.setEnabled(false);
      btnClearTraktData.setEnabled(false);
      btnClearTraktData.addActionListener(e -> {
        int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.clearmovies.hint"),
            BUNDLE.getString("Settings.trakt.clearmovies"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
        if (confirm == JOptionPane.YES_OPTION) {
          TmmTask task = new ClearTraktTvTask(true, false);
          TmmTaskManager.getInstance().addUnnamedTask(task);
        }
      });
    }
  }

  private void initComponents() {
    setLayout(new MigLayout("", "[25lp:n][]", "[][][20lp][][][][20lp:n][][][]"));
    {
      final JLabel lblUiT = new JLabel(BUNDLE.getString("Settings.ui")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblUiT, 1.16667, Font.BOLD);
      add(lblUiT, "cell 0 0 2 1");
    }
    {
      chckbxPersistUiFilters = new JCheckBox(BUNDLE.getString("Settings.movie.persistuifilter"));
      add(chckbxPersistUiFilters, "cell 1 1");
    }
    {
      final JLabel lblAutomaticTasksT = new JLabel(BUNDLE.getString("Settings.automatictasks")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblAutomaticTasksT, 1.16667, Font.BOLD);
      add(lblAutomaticTasksT, "cell 0 3 2 1");
    }
    {
      chckbxRenameAfterScrape = new JCheckBox(BUNDLE.getString("Settings.movie.automaticrename"));
      add(chckbxRenameAfterScrape, "flowx,cell 1 4");
    }
    {
      final JLabel lblAutomaticRenameHint = new JLabel(IconManager.HINT);
      lblAutomaticRenameHint.setToolTipText(BUNDLE.getString("Settings.movie.automaticrename.desc")); //$NON-NLS-1$
      add(lblAutomaticRenameHint, "cell 1 4");
    }
    {
      chckbxTraktSync = new JCheckBox(BUNDLE.getString("Settings.trakt"));
      add(chckbxTraktSync, "flowx,cell 1 5");
    }
    {
      btnClearTraktData = new JButton(BUNDLE.getString("Settings.trakt.clearmovies"));
      add(btnClearTraktData, "cell 1 5");
    }
    {
      final JLabel lblMiscT = new JLabel(BUNDLE.getString("Settings.misc")); //$NON-NLS-1$
      TmmFontHelper.changeFont(lblMiscT, 1.16667, Font.BOLD);
      add(lblMiscT, "cell 0 7 2 1");
    }
    {
      chckbxBuildImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimport")); //$NON-NLS-1$
      add(chckbxBuildImageCache, "flowx,cell 1 8");
    }
    {
      final JLabel lblBuildImageCacheHint = new JLabel(IconManager.HINT);
      lblBuildImageCacheHint.setToolTipText(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
      add(lblBuildImageCacheHint, "cell 1 8");
    }
    {
      chckbxRuntimeFromMi = new JCheckBox(BUNDLE.getString("Settings.runtimefrommediafile")); //$NON-NLS-1$
      add(chckbxRuntimeFromMi, "cell 1 9");
    }
  }

  protected void initDataBindings() {
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty = BeanProperty.create("storeUiFilters");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty, chckbxPersistUiFilters, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_1 = BeanProperty.create("movieRenameAfterScrape");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_1, chckbxRenameAfterScrape, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_2 = BeanProperty.create("syncTrakt");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_2, chckbxTraktSync, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_3 = BeanProperty.create("buildImageCacheOnImport");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_3, chckbxBuildImageCache, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<MovieSettings, Boolean> movieSettingsBeanProperty_4 = BeanProperty.create("runtimeFromMediaInfo");
    AutoBinding<MovieSettings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        movieSettingsBeanProperty_4, chckbxRuntimeFromMi, jCheckBoxBeanProperty);
    autoBinding_4.bind();
  }
}
