/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.AutoValue_MeterSharedState.Builder;

import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class MeterSharedState {
  static MeterSharedState create(InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return MeterSharedState.builder()
        .setInstrumentationLibraryInfo(instrumentationLibraryInfo)
        .setInstrumentRegistry(new InstrumentRegistry()).build();

  }

  abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  abstract InstrumentRegistry getInstrumentRegistry();

  abstract ImmutableSet<MetricsProcessor> getMetricsProcessors();

  public static Builder builder() {
    return new AutoValue_MeterSharedState.Builder();
  }

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract MeterSharedState build();

    public abstract ImmutableSet.Builder<MetricsProcessor> metricsProcessorsBuilder();

    public abstract Builder setInstrumentRegistry(InstrumentRegistry registry);

    public abstract Builder setInstrumentationLibraryInfo(InstrumentationLibraryInfo info);
  }
}
