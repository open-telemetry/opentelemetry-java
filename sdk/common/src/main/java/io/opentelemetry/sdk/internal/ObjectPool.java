/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.function.Supplier;

/**
 * A pool of objects of type {@code T}.
 *
 * <p>When an object is borrowed from an empty pool, an object will be created by the supplied
 * {@code objectCreator} and returned immediately. When the pool is not empty, an object is removed
 * from the pool and returned. The user is expected to return the object to the pool when it is no
 * longer used.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * <p>This class is not thread-safe.
 */
public final class ObjectPool<T> {
  private final ArrayBasedStack<T> pool;
  private final Supplier<T> objectCreator;

  /**
   * Constructs an object pool.
   *
   * @param objectCreator Supplier used to create an object when the pool is empty
   */
  public ObjectPool(Supplier<T> objectCreator) {
    this.pool = new ArrayBasedStack<>();
    this.objectCreator = objectCreator;
  }

  /**
   * Gets an object from the pool.
   *
   * @return An object from the pool, or a new object if the pool is empty
   */
  public T borrowObject() {
    T object = pool.pop();
    if (object == null) {
      object = objectCreator.get();
    }
    return object;
  }

  public void returnObject(T object) {
    pool.push(object);
  }
}
