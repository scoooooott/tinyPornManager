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
package org.tinymediamanager.ui.components;

import java.awt.Graphics;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.Person;

/**
 * The Class ActorImageLabel.
 * 
 * @author Manuel Laggner
 */
public class ActorImageLabel extends ImageLabel {

  private static final long         serialVersionUID = -1768796209645569296L;

  protected SwingWorker<Void, Void> actorWorker      = null;
  protected MediaEntity             mediaEntity      = null;
  protected Person                  actor            = null;

  public void setActor(MediaEntity mediaEntity, Person actor) {
    if (mediaEntity != null && actor != null && actor != this.actor) {
      this.mediaEntity = mediaEntity;
      this.actor = actor;

      scaledImage = null;
      this.repaint();
    }
  }

  @Override
  public void setImageUrl(String newValue) {
    String oldValue = this.imageUrl;
    this.imageUrl = newValue;
    firePropertyChange("imageUrl", oldValue, newValue);

    // stop previous worker
    if (worker != null && !worker.isDone()) {
      worker.cancel(true);
    }

    scaledImage = null;

    if (StringUtils.isEmpty(newValue)) {
      this.repaint();
      return;
    }

    // fetch image in separate worker -> performance
    // only do http fetches, if the label is visible
    if (isShowing()) {
      // stop previous worker
      if (worker != null && !worker.isDone()) {
        worker.cancel(true);
      }

      worker = new ImageFetcher(this.getSize());
      worker.execute();
      this.repaint();
    }
    else {
      scaledImage = null;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    // refetch the image if its visible now
    if (isShowing() && scaledImage == null && this.actor != null) {
      // load actors async
      if (actorWorker != null && !actorWorker.isDone()) {
        actorWorker.cancel(false);
      }

      // load image in separate worker -> performance
      actorWorker = new ActorImageLoader(actor);
      actorWorker.execute();
    }
    else if (isShowing() && scaledImage == null && StringUtils.isNotBlank(imageUrl)) {
      worker = new ImageFetcher(this.getSize());
      worker.execute();
      return;
    }

    super.paintComponent(g);
  }

  /*
   * inner class for loading the actor images
   */
  protected class ActorImageLoader extends SwingWorker<Void, Void> {
    private Person actor;
    private Path   imagePath = null;

    public ActorImageLoader(Person actor) {
      this.actor = actor;
    }

    @Override
    protected Void doInBackground() {
      // set file (or cached one) if existent
      String actorImageFilename = actor.getNameForStorage();
      if (StringUtils.isNotBlank(actorImageFilename)) {
        Path p = ImageCache.getCachedFile(Paths.get(mediaEntity.getPath(), Person.ACTOR_DIR, actorImageFilename));
        if (p != null) {
          imagePath = p;
          return null;
        }
      }

      // no file found, try to cache url (if visible, otherwise load on demand
      // in paintComponent)
      if (isShowing()) {
        Path p = ImageCache.getCachedFile(actor.getThumbUrl());
        if (p != null) {
          imagePath = p;
        }
      }

      return null;
    }

    @Override
    protected void done() {
      if (isCancelled()) {
        return;
      }

      if (imagePath != null) {
        setImagePath(imagePath.toString());
      }
      else if (StringUtils.isNotBlank(imageUrl)) {
        setImageUrl(actor.getThumbUrl());
      }
    }
  }
}
