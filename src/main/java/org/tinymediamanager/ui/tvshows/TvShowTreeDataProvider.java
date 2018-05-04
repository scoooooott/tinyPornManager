/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import java.beans.PropertyChangeListener;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.components.tree.TmmTreeDataProvider;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * The class TvShowTreeDataProvider is used for providing and managing the data for the TV show tree
 * 
 * @author Manuel Laggner
 */
public class TvShowTreeDataProvider extends TmmTreeDataProvider<TmmTreeNode> {
  private TmmTreeNode                  root           = new TmmTreeNode(new Object(), this);
  private RuleBasedCollator            stringCollator = (RuleBasedCollator) RuleBasedCollator.getInstance();

  private final PropertyChangeListener tvShowListPropertyChangeListener;
  private final PropertyChangeListener tvShowPropertyChangeListener;
  private final PropertyChangeListener episodePropertyChangeListener;

  private final TvShowList             tvShowList     = TvShowList.getInstance();

  public TvShowTreeDataProvider() {
    tvShowListPropertyChangeListener = evt -> {
      TvShow tvShow;

      switch (evt.getPropertyName()) {
        case Constants.ADDED_TV_SHOW:
          tvShow = (TvShow) evt.getNewValue();
          addTvShow(tvShow);
          break;

        case Constants.REMOVED_TV_SHOW:
          tvShow = (TvShow) evt.getNewValue();
          removeTvShow(tvShow);
          break;

        default:
          nodeChanged(evt.getSource());
          break;
      }
    };
    tvShowList.addPropertyChangeListener(tvShowListPropertyChangeListener);

    tvShowPropertyChangeListener = evt -> {
      TvShowSeason season;
      TvShowEpisode episode;

      switch (evt.getPropertyName()) {
        case Constants.ADDED_SEASON:
          season = (TvShowSeason) evt.getNewValue();
          addTvShowSeason(season);
          break;

        case Constants.ADDED_EPISODE:
          episode = (TvShowEpisode) evt.getNewValue();
          addTvShowEpisode(episode);
          break;

        case Constants.REMOVED_EPISODE:
          episode = (TvShowEpisode) evt.getNewValue();
          removeTvShowEpisode(episode);
          break;

        default:
          nodeChanged(evt.getSource());
          break;
      }
    };

    episodePropertyChangeListener = evt -> {
      TvShowEpisode episode;

      switch (evt.getPropertyName()) {
        // changed the season/episode nr of an episode
        case Constants.SEASON:
        case Constants.EPISODE:
          // simply remove it from the tree and readd it
          episode = (TvShowEpisode) evt.getSource();
          removeTvShowEpisode(episode);
          addTvShowEpisode(episode);

        default:
          nodeChanged(evt.getSource());
          break;
      }
    };

    setTreeComparator(new TvShowComparator());
  }

  /**
   * trigger a node changed event for all other events
   * 
   * @param source
   */
  private void nodeChanged(Object source) {
    TmmTreeNode node = getNodeFromCache(source);
    if (node != null) {
      firePropertyChange(NODE_CHANGED, null, node);
    }
  }

  @Override
  public TmmTreeNode getRoot() {
    return root;
  }

  @Override
  public TmmTreeNode getParent(TmmTreeNode child) {
    if (child.getUserObject() instanceof TvShow) {
      return root;
    }
    else if (child.getUserObject() instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) child.getUserObject();
      TmmTreeNode node = getNodeFromCache(season.getTvShow());
      // parent TV show not yet added? add it
      if (node == null) {
        node = addTvShow(season.getTvShow());
      }
      return node;
    }
    else if (child.getUserObject() instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) child.getUserObject();
      TmmTreeNode node = getNodeFromCache(episode.getTvShowSeason());
      if (node == null) {
        node = addTvShowSeason(episode.getTvShowSeason());
      }
      // also check if the TV show has already been added
      if (getNodeFromCache(episode.getTvShow()) == null) {
        addTvShow(episode.getTvShow());
      }
      return node;
    }
    return null;
  }

  @Override
  public List<TmmTreeNode> getChildren(TmmTreeNode parent) {
    if (parent == root) {
      ArrayList<TmmTreeNode> nodes = new ArrayList<>();
      for (TvShow tvShow : new ArrayList<>(tvShowList.getTvShows())) {
        TmmTreeNode node = new TvShowTreeNode(tvShow, this);
        putNodeToCache(tvShow, node);
        nodes.add(node);

        // add a propertychangelistener for this tv show
        tvShow.addPropertyChangeListener(tvShowPropertyChangeListener);
      }
      return nodes;
    }
    else if (parent.getUserObject() instanceof TvShow) {
      TvShow tvShow = (TvShow) parent.getUserObject();
      ArrayList<TmmTreeNode> nodes = new ArrayList<>();
      for (TvShowSeason season : tvShow.getSeasons()) {
        TmmTreeNode node = new TvShowSeasonTreeNode(season, this);
        putNodeToCache(season, node);
        nodes.add(node);
      }
      return nodes;
    }
    else if (parent.getUserObject() instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) parent.getUserObject();
      ArrayList<TmmTreeNode> nodes = new ArrayList<>();
      for (TvShowEpisode episode : season.getEpisodesForDisplay()) {
        TmmTreeNode node = new TvShowEpisodeTreeNode(episode, this);
        putNodeToCache(episode, node);
        nodes.add(node);

        // add a propertychangelistener for this episode
        episode.addPropertyChangeListener(episodePropertyChangeListener);
      }
      return nodes;
    }
    return null;
  }

  @Override
  public boolean isLeaf(TmmTreeNode node) {
    return node.getUserObject() instanceof TvShowEpisode;
  }

  @Override
  public Comparator<TmmTreeNode> getTreeComparator() {
    return super.getTreeComparator();
  }

  private TmmTreeNode addTvShow(TvShow tvShow) {
    // check if this tv show has already been added
    TmmTreeNode cachedNode = getNodeFromCache(tvShow);
    if (cachedNode != null) {
      return cachedNode;
    }

    // add a new node
    TmmTreeNode node = new TvShowTreeNode(tvShow, this);
    putNodeToCache(tvShow, node);
    firePropertyChange(NODE_INSERTED, null, node);

    // and also add a propertychangelistener to react on changes inside the tv show
    tvShow.addPropertyChangeListener(tvShowPropertyChangeListener);
    return node;
  }

  private void removeTvShow(TvShow tvShow) {
    TmmTreeNode cachedNode = removeNodeFromCache(tvShow);
    if (cachedNode == null) {
      return;
    }

    // remove all children from the map (the nodes will be removed by the treemodel)
    for (TvShowSeason season : tvShow.getSeasons()) {
      removeNodeFromCache(season);
    }
    for (TvShowEpisode epsiode : tvShow.getEpisodesForDisplay()) {
      removeNodeFromCache(epsiode);
    }

    // remove the propertychangelistener from this tv show
    tvShow.removePropertyChangeListener(tvShowPropertyChangeListener);

    firePropertyChange(NODE_REMOVED, null, cachedNode);
  }

  private TmmTreeNode addTvShowSeason(TvShowSeason season) {
    // check if this season has already been added
    TmmTreeNode cachedNode = getNodeFromCache(season);
    if (cachedNode != null) {
      return cachedNode;
    }

    // add a new node
    TmmTreeNode node = new TvShowSeasonTreeNode(season, this);
    putNodeToCache(season, node);
    firePropertyChange(NODE_INSERTED, null, node);
    return node;
  }

  private TmmTreeNode addTvShowEpisode(TvShowEpisode episode) {
    // check if this episode has already been added
    TmmTreeNode cachedNode = getNodeFromCache(episode);
    if (cachedNode != null) {
      return cachedNode;
    }

    // add a new node
    TmmTreeNode node = new TvShowEpisodeTreeNode(episode, this);
    putNodeToCache(episode, node);
    firePropertyChange(NODE_INSERTED, null, node);

    // and also add a propertychangelistener to react on changes inside the episode
    episode.addPropertyChangeListener(episodePropertyChangeListener);
    return node;
  }

  private void removeTvShowEpisode(TvShowEpisode episode) {
    TmmTreeNode cachedNode = removeNodeFromCache(episode);
    if (cachedNode == null) {
      return;
    }

    // remove the propertychangelistener from this episode
    episode.removePropertyChangeListener(episodePropertyChangeListener);

    firePropertyChange(NODE_REMOVED, null, cachedNode);
  }

  /*
   * helper classes
   */
  class TvShowComparator implements Comparator<TmmTreeNode> {
    @Override
    public int compare(TmmTreeNode o1, TmmTreeNode o2) {
      Object userObject1 = o1.getUserObject();
      Object userObject2 = o2.getUserObject();

      if (userObject1 instanceof TvShow && userObject2 instanceof TvShow) {
        TvShow tvShow1 = (TvShow) userObject1;
        TvShow tvShow2 = (TvShow) userObject2;
        if (stringCollator != null) {
          return stringCollator.compare(tvShow1.getTitleSortable().toLowerCase(), tvShow2.getTitleSortable().toLowerCase());
        }
        return tvShow1.getTitleSortable().compareToIgnoreCase(tvShow2.getTitleSortable());
      }

      if (userObject1 instanceof TvShowSeason && userObject2 instanceof TvShowSeason) {
        TvShowSeason tvShowSeason1 = (TvShowSeason) userObject1;
        TvShowSeason tvShowSeason2 = (TvShowSeason) userObject2;
        return tvShowSeason1.getSeason() - tvShowSeason2.getSeason();
      }

      if (userObject1 instanceof TvShowEpisode && userObject2 instanceof TvShowEpisode) {
        TvShowEpisode tvShowEpisode1 = (TvShowEpisode) userObject1;
        TvShowEpisode tvShowEpisode2 = (TvShowEpisode) userObject2;
        return tvShowEpisode1.getEpisode() - tvShowEpisode2.getEpisode();
      }

      return o1.toString().compareToIgnoreCase(o2.toString());
    }
  }

  class TvShowTreeNode extends TmmTreeNode {
    private static final long serialVersionUID = -1316609340104597133L;

    /**
     * Instantiates a new tv show tree node.
     * 
     * @param userObject
     *          the user object
     */
    public TvShowTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
      super(userObject, dataProvider);
    }

    /**
     * provides the right name of the node for display.
     * 
     * @return the string
     */
    @Override
    public String toString() {
      // return movieSet name
      if (getUserObject() instanceof TvShow) {
        TvShow tvShow = (TvShow) getUserObject();
        return tvShow.getTitle();
      }

      // fallback: call super
      return super.toString();
    }
  }

  class TvShowSeasonTreeNode extends TmmTreeNode {
    private static final long serialVersionUID = -5734830011018805194L;

    /**
     * Instantiates a new tv show season tree node.
     * 
     * @param userObject
     *          the user object
     */
    public TvShowSeasonTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
      super(userObject, dataProvider);
    }

    /**
     * provides the right name of the node for display
     */
    @Override
    public String toString() {
      // return movieSet name
      if (getUserObject() instanceof TvShowSeason) {
        TvShowSeason season = (TvShowSeason) getUserObject();
        if (season.getSeason() == -1) {
          return "Uncategorized";
        }

        return "Season " + season.getSeason();
      }

      // fallback: call super
      return super.toString();
    }
  }

  class TvShowEpisodeTreeNode extends TmmTreeNode {
    private static final long serialVersionUID = -7108614568808831980L;

    /**
     * Instantiates a new tv show episode tree node.
     * 
     * @param userObject
     *          the user object
     */
    public TvShowEpisodeTreeNode(Object userObject, TmmTreeDataProvider dataProvider) {
      super(userObject, dataProvider);
    }

    /**
     * provides the right name of the node for display.
     * 
     * @return the string
     */
    @Override
    public String toString() {
      // return episode name and number
      if (getUserObject() instanceof TvShowEpisode) {
        TvShowEpisode episode = (TvShowEpisode) getUserObject();
        if (episode.getEpisode() >= 0) {
          return episode.getEpisode() + ". " + episode.getTitle();
        }
        else {
          return episode.getTitle();
        }
      }

      // fallback: call super
      return super.toString();
    }
  }
}
