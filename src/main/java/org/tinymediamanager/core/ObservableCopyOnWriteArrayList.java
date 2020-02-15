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

package org.tinymediamanager.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

/**
 * this {@link List} in an observable {@link CopyOnWriteArrayList} to use with beansbinding
 *
 * @param <E>
 * @author Manuel Laggner
 */
public class ObservableCopyOnWriteArrayList<E> extends CopyOnWriteArrayList<E> implements ObservableList<E> {
  private List<ObservableListListener> listeners;

  public ObservableCopyOnWriteArrayList() {
    this.listeners = new CopyOnWriteArrayList<>();
  }

  public ObservableCopyOnWriteArrayList(Collection<? extends E> c) {
    super(c);
    this.listeners = new CopyOnWriteArrayList<>();
  }

  public E set(int index, E element) {
    E oldValue = super.set(index, element);

    Iterator iterator = this.listeners.iterator();
    while (iterator.hasNext()) {
      ObservableListListener listener = (ObservableListListener) iterator.next();
      listener.listElementReplaced(this, index, oldValue);
    }

    return oldValue;
  }

  public void add(int index, E element) {
    super.add(index, element);

    Iterator iterator = this.listeners.iterator();
    while (iterator.hasNext()) {
      ObservableListListener listener = (ObservableListListener) iterator.next();
      listener.listElementsAdded(this, index, 1);
    }

  }

  public E remove(int index) {
    E oldValue = super.remove(index);

    Iterator iterator = this.listeners.iterator();
    while (iterator.hasNext()) {
      ObservableListListener listener = (ObservableListListener) iterator.next();
      listener.listElementsRemoved(this, index, Collections.singletonList(oldValue));
    }

    return oldValue;
  }

  public boolean addAll(Collection<? extends E> c) {
    return this.addAll(this.size(), c);
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    if (super.addAll(index, c)) {

      Iterator iterator = this.listeners.iterator();
      while (iterator.hasNext()) {
        ObservableListListener listener = (ObservableListListener) iterator.next();
        listener.listElementsAdded(this, index, c.size());
      }
    }

    return false;
  }

  public void clear() {
    List<E> dup = new ArrayList(this);
    super.clear();

    if (!dup.isEmpty()) {
      Iterator iterator = this.listeners.iterator();
      while (iterator.hasNext()) {
        ObservableListListener listener = (ObservableListListener) iterator.next();
        listener.listElementsRemoved(this, 0, dup);
      }
    }
  }

  @Override
  public void addObservableListListener(ObservableListListener observableListListener) {
    this.listeners.add(observableListListener);
  }

  @Override
  public void removeObservableListListener(ObservableListListener observableListListener) {
    this.listeners.remove(observableListListener);
  }

  @Override
  public boolean supportsElementPropertyChanged() {
    return false;
  }
}
