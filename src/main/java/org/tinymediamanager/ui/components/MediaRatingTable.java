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
package org.tinymediamanager.ui.components;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.ui.NumberCellEditor;
import org.tinymediamanager.ui.components.table.TmmTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

/**
 * The class MediaRatingTable is used to display / edit ratings
 *
 * @author Manuel Laggner
 */
public class MediaRatingTable extends TmmTable {
  private static final long                                           serialVersionUID = 8010732881277204728L;
  /** @wbp.nls.resourceBundle messages */
  private static final ResourceBundle                                 BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control());

  private Map<String, org.tinymediamanager.core.entities.MediaRating> ratingMap;
  private EventList<MediaRating>                                      mediaRatingList;
  private boolean                                                     editable;

  /**
   * this constructor is used to display the ratings
   *
   * @param ratings
   *          a map containing the ratings
   */
  public MediaRatingTable(Map<String, org.tinymediamanager.core.entities.MediaRating> ratings) {
    this.ratingMap = ratings;
    this.editable = false;
    this.mediaRatingList = convertRatingMapToEventList(ratingMap, true);
    setModel(new DefaultEventTableModel<>(mediaRatingList, new MediaRatingTableFormat(editable)));
    setTableHeader(null);
    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
  }

  /**
   * this constructor is used to edit the ratings
   *
   * @param mediaRatings
   *          an eventlist containing the ratings
   */
  public MediaRatingTable(EventList<MediaRating> mediaRatings) {
    this.ratingMap = null;
    this.editable = true;
    this.mediaRatingList = mediaRatings;
    setModel(new DefaultEventTableModel<>(mediaRatingList, new MediaRatingTableFormat(editable)));
    // setTableHeader(null);
    putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    // value column
    TableColumn column = getColumnModel().getColumn(1);
    column.setCellEditor(new NumberCellEditor(3, 2));

    // maxValue column
    column = getColumnModel().getColumn(2);
    column.setCellEditor(new NumberCellEditor(3, 0));

    // votes column
    column = getColumnModel().getColumn(3);
    column.setCellEditor(new NumberCellEditor(10, 0));
  }

  public static EventList<MediaRating> convertRatingMapToEventList(Map<String, org.tinymediamanager.core.entities.MediaRating> idMap,
      boolean withUserRating) {
    EventList<MediaRating> idList = new BasicEventList<>();
    for (Entry<String, org.tinymediamanager.core.entities.MediaRating> entry : idMap.entrySet()) {
      if (org.tinymediamanager.core.entities.MediaRating.USER.equals(entry.getKey()) && !withUserRating) {
        continue;
      }

      MediaRating id = new MediaRating(entry.getKey());
      org.tinymediamanager.core.entities.MediaRating mediaRating = entry.getValue();

      id.value = mediaRating.getRating();
      id.votes = mediaRating.getVotes();
      id.maxValue = mediaRating.getMaxValue();

      idList.add(id);
    }

    return idList;
  }

  public static EventList<MediaRating> convertRatingMapToEventList(List<org.tinymediamanager.core.entities.MediaRating> ratings) {
    EventList<MediaRating> idList = new BasicEventList<>();
    for (org.tinymediamanager.core.entities.MediaRating rating : ratings) {
      MediaRating id = new MediaRating(rating.getId());
      org.tinymediamanager.core.entities.MediaRating mediaRating = rating;

      id.value = mediaRating.getRating();
      id.votes = mediaRating.getVotes();
      id.maxValue = mediaRating.getMaxValue();

      idList.add(id);
    }

    return idList;
  }

  public static class MediaRating {
    public String key;
    public float  value;
    public int    maxValue;
    public int    votes;

    public MediaRating(String key) {
      this.key = key;
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(13, 31).append(key).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MediaRating)) {
        return false;
      }
      if (obj == this) {
        return true;
      }
      MediaRating other = (MediaRating) obj;
      return StringUtils.equals(key, other.key);
    }
  }

  private class MediaRatingTableFormat implements WritableTableFormat<MediaRating> {
    private boolean editable;

    MediaRatingTableFormat(boolean editable) {
      this.editable = editable;
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return BUNDLE.getString("metatag.rating.source");

        case 1:
          return BUNDLE.getString("metatag.rating");

        case 2:
          return BUNDLE.getString("metatag.rating.maxvalue");

        case 3:
          return BUNDLE.getString("metatag.rating.votes");
      }
      return "";
    }

    @Override
    public boolean isEditable(MediaRating arg0, int arg1) {
      return editable;
    }

    @Override
    public Object getColumnValue(MediaRating arg0, int arg1) {
      if (arg0 == null) {
        return null;
      }
      switch (arg1) {
        case 0:
          return arg0.key;

        case 1:
          return arg0.value;

        case 2:
          return arg0.maxValue;

        case 3:
          return arg0.votes;
      }
      return null;
    }

    @Override
    public MediaRating setColumnValue(MediaRating arg0, Object arg1, int arg2) {
      if (arg0 == null || arg1 == null) {
        return null;
      }
      switch (arg2) {
        case 0:
          arg0.key = arg1.toString();
          break;

        case 1:
          try {
            arg0.value = (float) arg1;
          }
          catch (Exception ignored) {
          }
          break;

        case 2:
          try {
            arg0.maxValue = (int) arg1;
          }
          catch (Exception ignored) {
          }
          break;

        case 3:
          try {
            arg0.votes = (int) arg1;
          }
          catch (Exception ignored) {
          }
          break;
      }
      return arg0;
    }
  }
}
