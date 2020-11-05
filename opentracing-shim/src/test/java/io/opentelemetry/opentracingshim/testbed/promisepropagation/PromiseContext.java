/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.promisepropagation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

final class PromiseContext implements AutoCloseable {
  private final Phaser phaser;
  private final ExecutorService executor;

  public PromiseContext(Phaser phaser, int concurrency) {
    this.phaser = phaser;
    executor = Executors.newFixedThreadPool(concurrency);
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  public Future<?> submit(Runnable runnable) {
    phaser.register(); // register the work to be done on the executor
    return executor.submit(runnable);
  }

  public Phaser getPhaser() {
    return phaser;
  }
}
