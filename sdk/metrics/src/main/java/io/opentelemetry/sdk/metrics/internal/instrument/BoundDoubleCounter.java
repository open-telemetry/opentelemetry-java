/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.instrument;

import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.context.Context;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A counter instrument that records {@code double} values with pre-associated attributes.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public interface BoundDoubleCounter {
  /**
   * Records a value with pre-bound attributes.
   *
   * <p>Note: This may use {@code Context.current()} to pull the context associated with this
   * measurement.
   *
   * @param value The increment amount. MUST be non-negative.
   */
  void add(double value);

  /**
   * Records a value with pre-bound attributes.
   *
   * @param value The increment amount. MUST be non-negative.
   * @param context The explicit context to associate with this measurement.
   */
  void add(double value, Context context);

  /**
   * Unbinds the current bound instance from the {@link DoubleCounter}.
   *
   * <p>After this method returns the current instance is considered invalid (not being managed by
   * the instrument). This frees any reserved memory.
   */
  void unbind();
}
