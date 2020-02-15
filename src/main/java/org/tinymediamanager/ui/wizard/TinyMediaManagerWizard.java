/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.ui.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.tasks.MovieUpdateDatasourceTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.tasks.TvShowUpdateDatasourceTask;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * The class TinyMediaManagerWizard provides a wizard for easy first time setup of tinyMediaManager
 * 
 * @author Manuel Laggner
 */
public class TinyMediaManagerWizard extends TmmDialog {
  private static final long           serialVersionUID = 1112053710541745443L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<JPanel>                panels;
  private int                         activePanelIndex = 0;

  private JButton                     btnBack;
  private JButton                     btnNext;
  private JButton                     btnFinish;
  private JPanel                      panelContent;

  public TinyMediaManagerWizard() {
    super("tinyMediaManager Setup Wizard", "wizard");
    setBounds(5, 5, 800, 600);

    initComponents();

    // data init
    panels = new ArrayList<>();
    panels.add(new EntrancePanel());
    panels.add(new DisclaimerPanel());
    panels.add(new UiSettingsPanel());
    panels.add(new MovieSourcePanel());
    panels.add(new MovieScraperPanel());
    panels.add(new TvShowSourcePanel());
    panels.add(new TvShowScraperPanel());

    for (int i = 0; i < panels.size(); i++) {
      JPanel panel = panels.get(i);
      panelContent.add(panel, "" + i);
    }

    btnBack.setEnabled(false);
  }

  private void initComponents() {
    {
      JPanel panelSizing = new JPanel();
      getContentPane().add(panelSizing, BorderLayout.CENTER);
      panelSizing.setLayout(new MigLayout("", "[800lp,grow]", "[400lp,grow]"));

      panelContent = new JPanel();
      panelContent.setLayout(new CardLayout());
      panelSizing.add(panelContent, "cell 0 0,grow");
    }
    {
      btnBack = new JButton(new BackAction());
      addButton(btnBack);

      btnNext = new JButton(new NextAction());
      addButton(btnNext);

      btnFinish = new JButton(new FinishAction());
      addButton(btnFinish);
    }
  }

  @Override
  public void setVisible(boolean visible) {
    if (!visible) {
      TmmModuleManager.getInstance().saveSettings();
    }
    super.setVisible(visible);
  }

  @Override
  public void pack() {
    // do not pack - it would look weird
  }

  private class BackAction extends AbstractAction {
    private static final long serialVersionUID = -510135441507847318L;

    public BackAction() {
      putValue(NAME, BUNDLE.getString("wizard.back"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      activePanelIndex--;
      if (activePanelIndex == 0) {
        btnBack.setEnabled(false);
      }
      btnNext.setEnabled(true);
      btnFinish.setEnabled(false);
      CardLayout cl = (CardLayout) (panelContent.getLayout());
      cl.show(panelContent, "" + activePanelIndex);
    }
  }

  private class NextAction extends AbstractAction {
    private static final long serialVersionUID = -7813935881525980050L;

    public NextAction() {
      putValue(NAME, BUNDLE.getString("wizard.next"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      activePanelIndex++;
      if (panels.size() == activePanelIndex + 1) {
        btnNext.setEnabled(false);
        btnFinish.setEnabled(true);
      }
      btnBack.setEnabled(true);

      CardLayout cl = (CardLayout) (panelContent.getLayout());
      cl.show(panelContent, "" + activePanelIndex);
    }
  }

  private class FinishAction extends AbstractAction {
    private static final long serialVersionUID = 8047070989186510289L;

    public FinishAction() {
      putValue(NAME, BUNDLE.getString("wizard.finish"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      TinyMediaManagerWizard.this.setVisible(false);
      if (!MovieModuleManager.SETTINGS.getMovieDataSource().isEmpty()) {
        TmmThreadPool task = new MovieUpdateDatasourceTask();
        TmmTaskManager.getInstance().addMainTask(task);
      }
      if (!TvShowModuleManager.SETTINGS.getTvShowDataSource().isEmpty()) {
        TmmThreadPool task = new TvShowUpdateDatasourceTask();
        TmmTaskManager.getInstance().addMainTask(task);
      }
    }
  }
}
