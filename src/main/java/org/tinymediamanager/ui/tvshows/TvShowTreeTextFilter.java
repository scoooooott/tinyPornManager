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

package org.tinymediamanager.ui.tvshows;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;
import org.tinymediamanager.ui.components.tree.TmmTreeTextFilter;

public class TvShowTreeTextFilter<E extends TmmTreeNode> extends TmmTreeTextFilter {

  @Override
  public boolean accept(TmmTreeNode node) {
    if (StringUtils.isBlank(filterText)) {
      return true;
    }

    if (node instanceof TvShowTreeDataProvider.AbstractTvShowTreeNode) {
      TvShowTreeDataProvider.AbstractTvShowTreeNode treeNode = (TvShowTreeDataProvider.AbstractTvShowTreeNode) node;

      Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(filterText));

      // first: filter on the node
      Matcher matcher = pattern.matcher(treeNode.toString());
      if (matcher.find()) {
        return true;
      }

      // second: filter on the orignal title
      matcher = pattern.matcher(treeNode.getTitle());
      if (matcher.find()) {
        return true;
      }

      // third: filter on the original title
      matcher = pattern.matcher(treeNode.getOriginalTitle());
      if (matcher.find()) {
        return true;
      }

      // second: parse all children too
      for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements();) {
        if (accept((E) e.nextElement())) {
          return true;
        }
      }

      // third: check the parent(s)
      if (checkParent(node.getDataProvider().getParent(node), pattern)) {
        return true;
      }

      return false;
    }

    // no AbstractTvShowTreeNode? super call the accept from super
    return super.accept(node);
  }

  protected boolean checkParent(TmmTreeNode node, Pattern pattern) {
    if (node == null) {
      return false;
    }

    if (node instanceof TvShowTreeDataProvider.AbstractTvShowTreeNode) {
      TvShowTreeDataProvider.AbstractTvShowTreeNode treeNode = (TvShowTreeDataProvider.AbstractTvShowTreeNode) node;
      // first: filter on the node
      Matcher matcher = pattern.matcher(treeNode.toString());
      if (matcher.find()) {
        return true;
      }

      // second: filter on the orignal title
      matcher = pattern.matcher(treeNode.getTitle());
      if (matcher.find()) {
        return true;
      }

      // third: filter on the original title
      matcher = pattern.matcher(treeNode.getOriginalTitle());
      if (matcher.find()) {
        return true;
      }

      return checkParent(node.getDataProvider().getParent(node), pattern);
    }

    return super.checkParent(node, pattern);
  }
}
