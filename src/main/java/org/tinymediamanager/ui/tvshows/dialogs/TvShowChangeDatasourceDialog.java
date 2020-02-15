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

package org.tinymediamanager.ui.tvshows.dialogs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.tasks.TvShowChangeDatasourceTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.components.ReadOnlyTextArea;
import org.tinymediamanager.ui.components.TmmLabel;
import org.tinymediamanager.ui.dialogs.TmmDialog;

import net.miginfocom.swing.MigLayout;

/**
 * the class {@link TvShowChangeDatasourceDialog} is used to display the selection of the new datasource to be set to selected TV shows
 * 
 * @author Manuel Laggner
 */
public class TvShowChangeDatasourceDialog extends TmmDialog {
  private static final long           serialVersionUID = -1515243604267310274L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private List<TvShow>                tvShowsToEdit    = new ArrayList<>();

  private JComboBox<String>           cbDatasource;

  public TvShowChangeDatasourceDialog(final List<TvShow> tvShows) {
    super(BUNDLE.getString("tvshow.changedatasource"), "tvShowDatasourceEditor");

    tvShowsToEdit.addAll(tvShows);

    {
      JPanel panelContent = new JPanel();
      getContentPane().add(panelContent, BorderLayout.CENTER);
      panelContent.setLayout(new MigLayout("", "[][300lp,grow]", "[][20lp][][20lp]"));
      {
        JTextArea textArea = new ReadOnlyTextArea(BUNDLE.getString("changedatasource.hint.tvshow"));
        panelContent.add(textArea, "cell 0 0 2 1,grow");
      }
      {
        JLabel lblDatasourceT = new TmmLabel(BUNDLE.getString("changedatasource.newdatasource"));
        panelContent.add(lblDatasourceT, "cell 0 2");
      }
      {
        cbDatasource = new JComboBox(TvShowModuleManager.SETTINGS.getTvShowDataSource().toArray());
        panelContent.add(cbDatasource, "cell 1 2,growx,wmin 0");
      }

      /**********************************************************************************
       * ButtonPanel
       **********************************************************************************/
      {
        JButton cancelButton = new JButton(new DiscardAction());
        addButton(cancelButton);

        JButton okButton = new JButton(new ChangeDatasourceAction());
        addDefaultButton(okButton);
      }
    }
  }

  private class ChangeDatasourceAction extends AbstractAction {
    private static final long serialVersionUID = -3767744690599233490L;

    private ChangeDatasourceAction() {
      putValue(NAME, BUNDLE.getString("Button.ok"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("tvshow.changedatasource"));
      putValue(SMALL_ICON, IconManager.APPLY_INV);
      putValue(LARGE_ICON_KEY, IconManager.APPLY_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String newDatasource = (String) cbDatasource.getSelectedItem();
      if (StringUtils.isNotBlank(newDatasource)) {
        TmmThreadPool task = new TvShowChangeDatasourceTask(tvShowsToEdit, newDatasource);
        TmmTaskManager.getInstance().addMainTask(task);
      }

      setVisible(false);
    }
  }

  private class DiscardAction extends AbstractAction {
    private static final long serialVersionUID = -5581329896797961536L;

    private DiscardAction() {
      putValue(NAME, BUNDLE.getString("Button.cancel"));
      putValue(SHORT_DESCRIPTION, BUNDLE.getString("edit.discard"));
      putValue(SMALL_ICON, IconManager.CANCEL_INV);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
}
