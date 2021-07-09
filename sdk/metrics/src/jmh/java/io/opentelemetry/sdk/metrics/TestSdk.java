/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

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
          MeterProviderSharedState meterProviderSharedState =
              MeterProviderSharedState.create(
                  Clock.getDefault(),
                  Resource.empty(),
                  new ViewRegistry(
                      new EnumMap<InstrumentType, LinkedHashMap<Pattern, View>>(
                          InstrumentType.class)));
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

  private abstract static class SdkBuilder {
    abstract Meter build();
  }
}
