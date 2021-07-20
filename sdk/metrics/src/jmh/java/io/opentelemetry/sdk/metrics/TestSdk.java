/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;

@SuppressWarnings("ImmutableEnumChecker")
public enum TestSdk {
  API_ONLY(
      new SdkBuilder() {
        @Override
        Meter build() {
          return MeterProvider.noop().get("io.opentelemetry.sdk.metrics");
        }
      }),
  SDK(
      new SdkBuilder() {
        @Override
        Meter build() {
          return SdkMeterProvider.builder()
              .setClock(Clock.getDefault())
              .setResource(Resource.empty())
              .registerView(
                  InstrumentSelector.builder()
                      .setInstrumentNameRegex(".*histogram_recorder")
                      .setInstrumentType(InstrumentType.VALUE_RECORDER)
                      .build(),
                  // Histogram buckets the same as the metrics prototype/prometheus.
                  View.builder()
                      .setAggregatorFactory(
                          AggregatorFactory.histogram(
                              Arrays.<Double>asList(
                                  5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d,
                                  5_000d, 7_500d, 10_000d),
                              AggregationTemporality.CUMULATIVE))
                      .build())
              .build()
              .get("io.opentelemetry.sdk.metrics");
        }
      });

  private final SdkBuilder sdkBuilder;

  TestSdk(SdkBuilder sdkBuilder) {
    this.sdkBuilder = sdkBuilder;
  }

  public Meter getMeter() {
    return sdkBuilder.build();
  }

  private abstract static class SdkBuilder {
    abstract Meter build();
  }
}
