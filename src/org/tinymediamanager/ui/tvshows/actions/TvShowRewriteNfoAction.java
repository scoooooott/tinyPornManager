/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.ui.tvshows.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;

import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.tvshows.TvShowUIModule;

/**
 * The class TvShowRewriteNfoAction. To rewrite the NFOs of the selected TV shows
 * 
 * @author Manuel Laggner
 */
public class TvShowRewriteNfoAction extends AbstractAction {
  private static final long           serialVersionUID = -6575156436788397648L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  public TvShowRewriteNfoAction() {
    putValue(NAME, BUNDLE.getString("tvshow.rewritenfo")); //$NON-NLS-1$
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final List<TvShow> selectedTvShows = TvShowUIModule.getInstance().getSelectionModel().getSelectedTvShows();

    // rewrite selected NFOs
    TmmTaskManager.getInstance().addUnnamedTask(new Runnable() {
      @Override
      public void run() {
        for (TvShow tvShow : selectedTvShows) {
          tvShow.writeNFO();
        }
      }
    });
  }
}