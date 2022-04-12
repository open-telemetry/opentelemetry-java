/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** A reference to a batch callback registered via {@link BatchCallbackBuilder#build(Runnable)}. */
public interface BatchCallback extends AutoCloseable {

  /**
   * Remove the callback registered via {@link BatchCallbackBuilder#build(Runnable)}. After this is
   * called, the callback won't be invoked on future collections. Subsequent calls to {@link
   * #close()} have no effect.
   */
  @Override
  default void close() {}
}
