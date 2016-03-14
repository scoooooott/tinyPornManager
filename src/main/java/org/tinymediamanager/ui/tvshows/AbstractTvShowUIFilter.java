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
package org.tinymediamanager.ui.tvshows;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.event.SwingPropertyChangeSupport;

import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.ui.AbstractTmmUIFilter;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.components.tree.ITmmTreeFilter;
import org.tinymediamanager.ui.components.tree.TmmTreeNode;

/**
 * An abstract implementation for easier usage of the ITmmUIFilter and ITvShowUIFilter
 * 
 * @author Manuel Laggner
 */
public abstract class AbstractTvShowUIFilter extends AbstractTmmUIFilter implements ITvShowUIFilter<TmmTreeNode> {
  protected final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this, true);
  protected static final ResourceBundle BUNDLE                = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  @Override
  public boolean accept(TmmTreeNode node) {
    // is this filter active?
    if (!checkBox.isSelected()) {
      return true;
    }

    Object userObject = node.getUserObject();

    if (userObject instanceof TvShow) {
      TvShow tvShow = (TvShow) userObject;
      return accept(tvShow, new ArrayList<TvShowEpisode>(tvShow.getEpisodes()));
    }
    else if (userObject instanceof TvShowSeason) {
      TvShowSeason season = (TvShowSeason) userObject;
      return accept(season.getTvShow(), new ArrayList<TvShowEpisode>(season.getEpisodes()));
    }
    else if (userObject instanceof TvShowEpisode) {
      TvShowEpisode episode = (TvShowEpisode) userObject;
      return accept(episode.getTvShow(), Arrays.asList(episode));
    }

    return true;
  }

  /**
   * should we accept the node providing this data?
   * 
   * @param tvShow
   *          the tvShow of this node
   * @param episodes
   *          all episodes of this node
   * @return
   */
  protected abstract boolean accept(TvShow tvShow, List<TvShowEpisode> episodes);

  /**
   * delegate the filter changed event to the tree
   */
  @Override
  protected void filterChanged() {
    firePropertyChange(ITmmTreeFilter.TREE_FILTER_CHANGED, checkBox.isSelected(), !checkBox.isSelected());
  }

  /**
   * Adds the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Adds the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Removes the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  @Override
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  /**
   * Fire property change.
   * 
   * @param propertyName
   *          the property name
   * @param oldValue
   *          the old value
   * @param newValue
   *          the new value
   */
  protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
    propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
  }

  /**
   * Fire property change.
   * 
   * @param evt
   *          the evt
   */
  protected void firePropertyChange(PropertyChangeEvent evt) {
    propertyChangeSupport.firePropertyChange(evt);
  }
}
