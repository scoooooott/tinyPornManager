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
package org.tinymediamanager.ui.tvshows.filters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.MediaAiredStatus;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a status filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowStatusFilter extends AbstractCheckComboBoxTvShowUIFilter<MediaAiredStatus> {

  public TvShowStatusFilter() {
    super();
    buildAndInstallCertificationArray();
  }

  @Override
  public String getId() {
    return "tvShowStatus";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<MediaAiredStatus> airedStatuses = checkComboBox.getSelectedItems();

    return invert ^ airedStatuses.contains(tvShow.getStatus());
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.status"));
  }

  private void buildAndInstallCertificationArray() {
    List<MediaAiredStatus> airedStatuses = Arrays.asList(MediaAiredStatus.values());
    Collections.sort(airedStatuses);

    setValues(airedStatuses);
  }

  @Override
  protected String parseTypeToString(MediaAiredStatus type) throws Exception {
    return type.name();
  }

  @Override
  protected MediaAiredStatus parseStringToType(String string) throws Exception {
    return MediaAiredStatus.valueOf(string);
  }
}
