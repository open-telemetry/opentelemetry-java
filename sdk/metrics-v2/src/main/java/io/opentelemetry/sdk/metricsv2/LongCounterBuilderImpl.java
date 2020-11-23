/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

class LongCounterBuilderImpl implements LongCounter.Builder {

  private final Accumulator accumulator;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final String name;
  private String description;
  private String unit;

  LongCounterBuilderImpl(
      Accumulator accumulator, InstrumentationLibraryInfo instrumentationLibraryInfo, String name) {
    this.accumulator = accumulator;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.name = name;
  }

  @Override
  public LongCounterBuilderImpl setDescription(String description) {
    this.description = description;
    return this;
  }

  @Override
  public LongCounterBuilderImpl setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  @Override
  public LongCounter build() {
    return new LongCounterImpl(accumulator, instrumentationLibraryInfo, name, description, unit);
  }
}
