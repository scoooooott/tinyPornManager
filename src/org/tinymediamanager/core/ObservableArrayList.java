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
package org.tinymediamanager.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

/**
 * The class ObservableArrayList. An ArrayList which has the ability to inform listeners of changes (beansbinding)
 * 
 * @author Manuel Laggner
 */
public class ObservableArrayList<E> extends ArrayList<E> implements ObservableList<E> {
  private static final long            serialVersionUID = 3703038527571217084L;
  private List<ObservableListListener> listeners;

  public ObservableArrayList() {
    listeners = new CopyOnWriteArrayList<ObservableListListener>();
  }

  @Override
  public E set(int index, E element) {
    E oldValue = super.set(index, element);

    for (ObservableListListener listener : listeners) {
      listener.listElementReplaced(this, index, oldValue);
    }

    return oldValue;
  }

  @Override
  public void add(int index, E element) {
    super.add(index, element);

    for (ObservableListListener listener : listeners) {
      listener.listElementsAdded(this, index, 1);
    }
  }

  @Override
  public E remove(int index) {
    E oldValue = super.remove(index);

    for (ObservableListListener listener : listeners) {
      listener.listElementsRemoved(this, index, java.util.Collections.singletonList(oldValue));
    }

    return oldValue;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return addAll(size(), c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    if (super.addAll(index, c)) {
      for (ObservableListListener listener : listeners) {
        listener.listElementsAdded(this, index, c.size());
      }
    }

    return false;
  }

  @Override
  public void clear() {
    List<E> dup = new ArrayList<E>(this);
    super.clear();

    if (dup.size() != 0) {
      for (ObservableListListener listener : listeners) {
        listener.listElementsRemoved(this, 0, dup);
      }
    }
  }

  @Override
  public void addObservableListListener(ObservableListListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeObservableListListener(ObservableListListener listener) {
    listeners.remove(listener);
  }

  @Override
  public boolean supportsElementPropertyChanged() {
    return false;
  }
}
