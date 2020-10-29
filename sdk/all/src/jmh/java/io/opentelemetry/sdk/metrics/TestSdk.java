/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.errorprone.annotations.Immutable;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;

public enum TestSdk {
  API_ONLY(
      new SdkBuilder() {
        @Override
        Meter build() {
          return Meter.getDefault();
        }
      }),
  SDK(
      new SdkBuilder() {
        @Override
        Meter build() {
          MeterProviderSharedState meterProviderSharedState =
              MeterProviderSharedState.create(MillisClock.getInstance(), Resource.getEmpty());
          InstrumentationLibraryInfo instrumentationLibraryInfo =
              InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics", null);

          return new MeterSdk(
              meterProviderSharedState, instrumentationLibraryInfo, new ViewRegistry());
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
