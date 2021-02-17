/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.errorprone.annotations.Immutable;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.resources.Resource;

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
          MeterProviderSharedState meterProviderSharedState =
              MeterProviderSharedState.create(SystemClock.getInstance(), Resource.empty());
          InstrumentationLibraryInfo instrumentationLibraryInfo =
              InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics", null);

          return new SdkMeter(meterProviderSharedState, instrumentationLibraryInfo);
        }
      });

  private final SdkBuilder sdkBuilder;

  TestSdk(SdkBuilder sdkBuilder) {
    this.sdkBuilder = sdkBuilder;
  }

  public Meter getMeter() {
    return sdkBuilder.build();
  }

  @Immutable
  private abstract static class SdkBuilder {
    abstract Meter build();
  }
}
