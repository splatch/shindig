/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.common.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.shindig.api.cache.Cache;

/**
 * A basic LRU cache. Prefer using EhCache for most purposes to this class.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {
  final int capacity;

  public LruCache(int capacity) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  public synchronized V getElement(K key) {
    return super.get(key);
  }

  public synchronized void addElement(K key, V value) {
    super.put(key, value);
  }

  public synchronized V removeElement(K key) {
    return super.remove(key);
  }

  public long getCapacity() {
    return capacity;
  }

  public long getSize() {
    return size();
  }

  @Override
  protected synchronized boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > capacity;
  }
}
