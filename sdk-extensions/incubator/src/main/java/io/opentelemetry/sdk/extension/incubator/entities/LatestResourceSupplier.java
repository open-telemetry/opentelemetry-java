/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A supplier of resource which listenes to Entity events.
 *
 * <p>This class will wait for availability via ResourceInitialized event before returning from any
 * `getResource` call.
 */
public class LatestResourceSupplier implements EntityListener, Supplier<Resource> {

  private final AtomicReference<Resource> current = new AtomicReference<>(null);
  private final Object initializationLock = new Object();
  private final long maxStartupDelayMs;

  public LatestResourceSupplier(long maxStartupDelayMs) {
    this.maxStartupDelayMs = maxStartupDelayMs;
  }

  @Override
  public void onResourceInit(Resource resource) {
    current.lazySet(resource);
    // Here we can notify anyone waiting on initialization.
    synchronized (initializationLock) {
      initializationLock.notifyAll();
    }
  }

  @Override
  public void onEntityState(EntityState state, Resource resource) {
    current.lazySet(resource);
  }

  @Override
  public void onEntityDelete(EntityState state, Resource resource) {
    current.lazySet(resource);
  }

  @Override
  public Resource get() {
    Resource result = this.current.get();
    if (result == null) {
      synchronized (initializationLock) {
        result = this.current.get();
        long startTime = System.currentTimeMillis();
        boolean stillWaiting = true;
        while (result == null || stillWaiting) {
          long elapsedTime = System.currentTimeMillis() - startTime;
          long remainingTime = maxStartupDelayMs - elapsedTime;
          if (remainingTime <= 0) {
            stillWaiting = false;
            break;
          }
          try {
            initializationLock.wait(remainingTime);
          } catch (InterruptedException e) {
            break;
          }
        }
        if (result == null) {
          result = Resource.getDefault();
        }
      }
    }
    return result;
  }
}
