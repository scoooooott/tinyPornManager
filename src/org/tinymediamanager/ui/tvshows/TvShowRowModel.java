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
package org.tinymediamanager.ui.tvshows;

import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.swing.outline.RowModel;
import org.tinymediamanager.core.tvshow.TvShow;

/**
 * @author Manuel Laggner
 * 
 */
public class TvShowRowModel implements RowModel {

  @Override
  public Class getColumnClass(int column) {
    switch (column) {
      case 0:
      case 1:
        return String.class;
      default:
        assert false;
    }
    return null;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "S";

      case 1:
        return "E";

      default:
        assert false;
    }

    return null;
  }

  @Override
  public Object getValueFor(Object obj, int column) {
    if (!(obj instanceof TvShowTreeNode)) {
      return null;
    }

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
    TvShow tvShow = (TvShow) node.getUserObject();

    switch (column) {
      case 0:
        return tvShow.getSeasons().size();

      case 1:
        return tvShow.getEpisodes().size();

      default:
        assert false;
    }
    return null;
  }

  @Override
  public boolean isCellEditable(Object node, int column) {
    return false;
  }

  @Override
  public void setValueFor(Object node, int column, Object value) {
    // do nothing for now
  }
}
