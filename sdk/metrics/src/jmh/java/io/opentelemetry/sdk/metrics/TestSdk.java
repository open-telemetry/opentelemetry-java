/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;

@SuppressWarnings("ImmutableEnumChecker")
public enum TestSdk {
  API_ONLY(
      new SdkBuilder() {
        @Override
        Meter build() {
          return MeterProvider.noop().get("io.opentelemetry.sdk.metrics");
        }
      }),
  SDK_NO_EXEMPLARS(
      new SdkBuilder() {
        @Override
        Meter build() {
          return SdkMeterProvider.builder()
              .setClock(Clock.getDefault())
              .setResource(Resource.empty())
              .setExemplarSampler(
                  ExemplarSampler.builder().setFilter(ExemplarFilter.neverSample()).build())
              .build()
              .get("io.opentelemetry.sdk.metrics");
        }
      }),
  SDK(
      new SdkBuilder() {
        @Override
        Meter build() {
          return SdkMeterProvider.builder()
              .setClock(Clock.getDefault())
              .setResource(Resource.empty())
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

  public Tracer getTracer() {
    return sdkBuilder.buildTracer();
  }

  private abstract static class SdkBuilder {
    abstract Meter build();

    protected Tracer buildTracer() {
      return SdkTracerProvider.builder()
          .setSampler(Sampler.alwaysOn())
          .build()
          .get("io.opentelemetry.sdk.metrics");
    }
  }
}
