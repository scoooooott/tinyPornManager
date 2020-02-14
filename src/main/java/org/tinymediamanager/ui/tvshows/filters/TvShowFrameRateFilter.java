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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * This class implements a frame rate filter for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowFrameRateFilter extends AbstractCheckComboBoxTvShowUIFilter<Double> {
  private TvShowList tvShowList = TvShowList.getInstance();

  public TvShowFrameRateFilter() {
    super();
    buildAndInstallCodecArray();
    PropertyChangeListener propertyChangeListener = evt -> buildAndInstallCodecArray();
    tvShowList.addPropertyChangeListener(Constants.FRAME_RATE, propertyChangeListener);
  }

  @Override
  public String getId() {
    return "tvShowFrameRate";
  }

  @Override
  protected boolean accept(TvShow tvShow, List<TvShowEpisode> episodes, boolean invert) {
    List<Double> frameRates = checkComboBox.getSelectedItems();

    for (Double frameRate : frameRates) {
      if (invert ^ frameRate == 0) {
        return true;
      }

      // search codec in the episodes
      for (TvShowEpisode episode : episodes) {
        if (invert ^ frameRate == episode.getMediaInfoFrameRate()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.framerate"));
  }

  private void buildAndInstallCodecArray() {
    List<Double> frameRates = new ArrayList<>(tvShowList.getFrameRatesInEpisodes());
    Collections.sort(frameRates);

    setValues(frameRates);
  }

  @Override
  protected String parseTypeToString(Double type) throws Exception {
    return type.toString();
  }

  @Override
  protected Double parseStringToType(String string) throws Exception {
    return Double.parseDouble(string);
  }
}
