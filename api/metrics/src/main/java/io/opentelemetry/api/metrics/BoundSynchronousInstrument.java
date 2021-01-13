/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

interface BoundSynchronousInstrument {
  /**
   * Unbinds the current {@code Bound} from the Instrument.
   *
   * <p>After this method returns the current instance {@code Bound} is considered invalid (not
   * being managed by the instrument).
   */
  void unbind();
}
