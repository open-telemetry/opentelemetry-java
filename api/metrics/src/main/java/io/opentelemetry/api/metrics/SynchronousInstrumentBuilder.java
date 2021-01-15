/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/** Builder class for {@link SynchronousInstrument}. */
public interface SynchronousInstrumentBuilder extends InstrumentBuilder {
  @Override
  SynchronousInstrument<?> build();
}
