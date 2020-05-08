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
package org.tinymediamanager.core.tvshow;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Locale;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class {@link TvShowComparator} is used to (initial) sort the TV shows.
 * 
 * @author Manuel Laggner
 */
public class TvShowComparator implements Comparator<TvShow> {
  private Collator stringCollator;

  public TvShowComparator() {
    RuleBasedCollator defaultCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();
    try {
      // default collator ignores whitespaces
      // using hack from http://stackoverflow.com/questions/16567287/java-collation-ignores-space
      stringCollator = new RuleBasedCollator(defaultCollator.getRules().replace("<'\u005f'", "<' '<'\u005f'"));
    }
    catch (Exception e) {
      stringCollator = defaultCollator;
    }
  }

  @Override
  public int compare(TvShow tvShow1, TvShow tvShow2) {
    if (stringCollator != null) {
      String titleTvShow1 = StrgUtils.normalizeString(tvShow1.getTitleSortable().toLowerCase(Locale.ROOT));
      String titleTvShow2 = StrgUtils.normalizeString(tvShow2.getTitleSortable().toLowerCase(Locale.ROOT));
      return stringCollator.compare(titleTvShow1, titleTvShow2);
    }
    return tvShow1.getTitleSortable().toLowerCase(Locale.ROOT).compareTo(tvShow2.getTitleSortable().toLowerCase(Locale.ROOT));
  }
}
