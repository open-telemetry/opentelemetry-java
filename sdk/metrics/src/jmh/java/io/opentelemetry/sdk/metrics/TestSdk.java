/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

@SuppressWarnings("ImmutableEnumChecker")
public enum TestSdk {
  API_ONLY(
      new SdkBuilder() {
        @Override
        Meter buildMeter() {
          return MeterProvider.noop().meterBuilder("io.opentelemetry.sdk.metrics").build();
        }
      }),
  SDK(
      new SdkBuilder() {
        @Override
        Meter buildMeter() {
          return SdkMeterProvider.builder()
              .setResource(Resource.empty())
              .setClock(Clock.getDefault())
              .build()
              .meterBuilder("io.opentelemetry.sdk.metrics")
              .build();
        }
      }),
  SDK_WITH_EXEMPLAR(
      new SdkBuilder() {
        @Override
        Meter buildMeter() {
          return SdkMeterProvider.builder()
              .setResource(Resource.empty())
              .setClock(Clock.getDefault())
              .setMeasurementProcessor(
                  DefaultMeasurementProcessor.builder()
                      .setDefaultExemplarSampler(ExemplarSampler.WITH_SAMPLED_TRACES)
                      .build())
              .build()
              .meterBuilder("io.opentelemetry.sdk.metrics")
              .build();
        }
      });

  private final SdkBuilder sdkBuilder;

  TestSdk(SdkBuilder sdkBuilder) {
    this.sdkBuilder = sdkBuilder;
  }

  public Meter getMeter() {
    return sdkBuilder.buildMeter();
  }

  public Tracer getTracer() {
    return sdkBuilder.buildTracer();
  }

  private abstract static class SdkBuilder {
    abstract Meter buildMeter();

    protected Tracer buildTracer() {
      return SdkTracerProvider.builder()
          .setSampler(Sampler.alwaysOn())
          .build()
          .get("io.opentelemetry.sdk.metrics");
    }
  }
}
