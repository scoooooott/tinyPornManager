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
package org.tinymediamanager.ui.movies.filters;

import java.util.List;

import javax.swing.JLabel;

import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.ui.components.TmmLabel;

/**
 * this class is used for a video aspect ratio movie filter
 * 
 * @author Manuel Laggner
 */
public class MovieAspectRatioFilter extends AbstractCheckComboBoxMovieUIFilter<String> {
  private static final String RATIO_1_33 = "4:3 (1.33:1)";
  private static final String RATIO_1_37 = "11:8 (1.37:1)";
  private static final String RATIO_1_43 = "IMAX (1.43:1)";
  private static final String RATIO_1_56 = "14:9 (1.56:1)";
  private static final String RATIO_1_66 = "5:3 (1.66:1)";
  private static final String RATIO_1_78 = "16:9 (1.77:1)";
  private static final String RATIO_1_85 = "Widescreen (1.85:1)";
  private static final String RATIO_1_90 = "Digital IMAX (1.90:1)";
  private static final String RATIO_2_20 = "70mm (2.20:1)";
  private static final String RATIO_2_35 = "Anamorphic (2.35:1)";

  private final String        others;

  public MovieAspectRatioFilter() {
    super();
    others = BUNDLE.getString("filter.others");

    setValues(RATIO_1_33, RATIO_1_37, RATIO_1_43, RATIO_1_56, RATIO_1_66, RATIO_1_78, RATIO_1_85, RATIO_1_90, RATIO_2_20, RATIO_2_35, others);

  }

  @Override
  public String getId() {
    return "movieAspectRatio";
  }

  @Override
  public boolean accept(Movie movie) {
    List<String> selectedValues = checkComboBox.getSelectedItems();

    MediaFile mf = movie.getMainVideoFile();
    if (mf == null) {
      return false;
    }

    float aspectRatio = mf.getAspectRatio();

    for (String ratios : selectedValues) {
      if (aspectRatio == 1.33F) {
        if (ratios.equals(RATIO_1_33)) {
          return true;
        }
      }
      else if (aspectRatio == 1.37F) {
        if (ratios.equals(RATIO_1_37)) {
          return true;
        }
      }
      else if (aspectRatio == 1.43F) {
        if (ratios.equals(RATIO_1_43)) {
          return true;
        }
      }
      else if (aspectRatio == 1.56F) {
        if (ratios.equals(RATIO_1_56)) {
          return true;
        }
      }
      else if (aspectRatio == 1.66F) {
        if (ratios.equals(RATIO_1_66)) {
          return true;
        }
      }
      else if (aspectRatio == 1.78F) {
        if (ratios.equals(RATIO_1_78)) {
          return true;
        }
      }
      else if (aspectRatio == 1.85F) {
        if (ratios.equals(RATIO_1_85)) {
          return true;
        }
      }
      else if (aspectRatio == 1.90F) {
        if (ratios.equals(RATIO_1_90)) {
          return true;
        }
      }
      else if (aspectRatio == 2.20F) {
        if (ratios.equals(RATIO_2_20)) {
          return true;
        }
      }
      else if (aspectRatio == 2.35F) {
        if (ratios.equals(RATIO_2_35)) {
          return true;
        }
      }
      else if (ratios.equals(others)) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected JLabel createLabel() {
    return new TmmLabel(BUNDLE.getString("metatag.aspect"));
  }

  @Override
  protected String parseTypeToString(String type) throws Exception {
    return type;
  }

  @Override
  protected String parseStringToType(String string) throws Exception {
    return string;
  }
}
