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
package org.tinymediamanager.ui.components;

import java.awt.Container;
import java.awt.Graphics;
import java.io.File;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.movie.Movie;
import org.tinymediamanager.core.movie.MovieActor;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The Class ActorImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ActorImageLabel extends ImageLabel {

  private static final long           serialVersionUID = -1768796209645569296L;
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private Movie                       movie;

  public ActorImageLabel() {
    super();
    setAlternativeText(BUNDLE.getString("image.notfound.thumb")); //$NON-NLS-1$
  }

  public void setMovie(Movie movie) {
    this.movie = movie;
  }

  public void setActor(MovieActor actor) {
    if (actor != null) {
      if (movie != null && StringUtils.isNotEmpty(actor.getThumbPath())) {
        File actorThumb = new File(movie.getPath() + File.separator + actor.getThumbPath());
        if (actorThumb.exists()) {
          setImagePath(actorThumb.getPath());
          return;
        }
      }

      setImageUrl(actor.getThumb());
    }
  }

  @Override
  public void setImagePath(String newValue) {
    String oldValue = this.imagePath;

    if (StringUtils.isNotEmpty(oldValue) && oldValue.equals(newValue)) {
      return;
    }

    this.imagePath = newValue;
    firePropertyChange("imagePath", oldValue, newValue);

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    // load image in separate worker -> performance
    worker = new ImageLoader(this.imagePath);
    worker.execute();
  }

  @Override
  public void setImageUrl(String newValue) {
    String oldValue = this.imageUrl;
    this.imageUrl = newValue;
    firePropertyChange("imageUrl", oldValue, newValue);

    if (StringUtils.isEmpty(newValue)) {
      originalImage = null;
      this.repaint();
      return;
    }

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    // fetch image in separate worker -> performance
    // only do http fetches, if the label is visible
    if (amIVisible()) {
      worker = new ImageFetcher();
      worker.execute();
    }
    else {
      originalImage = null;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    // refetch the image if its visible now
    if (amIVisible() && originalImage == null && StringUtils.isNotBlank(imageUrl)) {
      worker = new ImageFetcher();
      worker.execute();
      return;
    }

    super.paintComponent(g);
  }

  /*
   * determine if the component is visible to the user
   */
  private boolean amIVisible() {
    Container c = getParent();
    while (c != null)
      if (!c.isVisible())
        return false;
      else
        c = c.getParent();
    return true;
  }
}
