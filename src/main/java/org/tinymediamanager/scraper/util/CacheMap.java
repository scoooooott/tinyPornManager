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
package org.tinymediamanager.scraper.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The class CacheMap is used to cache certain key/value pairs.
 * 
 * @param <K>
 *          the key of the map
 * @param <T>
 *          the value
 */
public class CacheMap<K, T> {
  protected long                timeToLive;
  protected Map<K, CacheObject> cacheMap;
  protected ReadWriteLock       readWriteLock = new ReentrantReadWriteLock();

  /**
   * inner class for managing the cache entries
   */
  protected class CacheObject {
    public long lastAccessed = System.currentTimeMillis();
    public T    value;

    protected CacheObject(T value) {
      this.value = value;
    }
  }

  /**
   * Constructs a new CacheMap
   * 
   * @param timeToLive
   *          time to live in the map (in seconds)
   * @param cleanupInterval
   *          cache cleaning interval (in seconds)
   */
  public CacheMap(final long timeToLive, final long cleanupInterval) {
    this.timeToLive = timeToLive;
    this.cacheMap = new HashMap<K, CacheObject>();

    // the thread for cleanup
    if (timeToLive > 0 && cleanupInterval > 0) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          while (true) {
            try {
              Thread.sleep(cleanupInterval * 1000);
            }
            catch (InterruptedException ex) {
              break;
            }
            cleanup();
          }
        }
      });

      t.setDaemon(true);
      t.start();
    }
  }

  /**
   * Put a new object to the map
   * 
   * @param key
   *          the key of the entry
   * @param value
   *          the value of the entry
   */
  public void put(K key, T value) {
    readWriteLock.writeLock().lock();
    cacheMap.put(key, new CacheObject(value));
    readWriteLock.writeLock().unlock();
  }

  /**
   * Get the specified object from the map (or null if no object is found)
   * 
   * @param key
   *          the key of the entry to get
   * @return the entry (if found) or null
   */
  public T get(K key) {
    readWriteLock.readLock().lock();
    CacheObject c = cacheMap.get(key);
    readWriteLock.readLock().unlock();

    if (c == null) {
      return null;
    }
    else {
      c.lastAccessed = System.currentTimeMillis();
      return c.value;
    }
  }

  public Set<K> keySet() {
    return cacheMap.keySet();
  }

  /**
   * Removes the specified object from the map
   * 
   * @param key
   *          the key of the entry
   */
  public void remove(K key) {
    readWriteLock.writeLock().lock();
    cacheMap.remove(key);
    readWriteLock.writeLock().unlock();
  }

  /**
   * Get the actual size of the map
   * 
   * @return the actual size of the map
   */
  public int size() {
    readWriteLock.readLock().lock();
    int size = cacheMap.size();
    readWriteLock.readLock().unlock();
    return size;
  }

  /**
   * Cleanup the map checking the time to live for each entry
   */
  public void cleanup() {
    cleanup(false);
  }

  /**
   * Cleanup the map checking the time to live for each entry or force cleaning
   * 
   * @param force
   *          force cleanup
   */
  public void cleanup(boolean force) {
    long now = System.currentTimeMillis();
    ArrayList<K> deleteKey = new ArrayList<>((cacheMap.size() / 2) + 1);

    readWriteLock.writeLock().lock();
    for (Map.Entry<K, CacheObject> entry : cacheMap.entrySet()) {
      K key = entry.getKey();
      CacheObject c = entry.getValue();

      if (c != null && ((now > (timeToLive * 1000 + c.lastAccessed)) || force)) {
        deleteKey.add(key);
      }
    }

    for (K key : deleteKey) {
      cacheMap.remove(key);
      Thread.yield();
    }
    readWriteLock.writeLock().unlock();
  }
}
