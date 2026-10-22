/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class ExtendedOpenTelemetrySdkTest {

  private static final Resource RESOURCE_A =
      Resource.create(Attributes.of(AttributeKey.stringKey("a"), "1"));
  private static final Resource RESOURCE_B =
      Resource.create(Attributes.of(AttributeKey.stringKey("b"), "2"));

  @Test
  void getSdkResourceProvider_defaultResources_returnsAutoWrapped() {
    ExtendedOpenTelemetrySdk sdk = buildExtended(null, null, null, null, null, null);

    assertThat(sdk.getSdkResourceProvider().getResource()).isEqualTo(Resource.getDefault());
  }

  @Test
  void getSdkResourceProvider_sameSdkResourceProviderOnAll_returnsIt() {
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(RESOURCE_A);
    ExtendedOpenTelemetrySdk sdk =
        buildExtended(null, resourceProvider, null, resourceProvider, null, resourceProvider);

    assertThat(sdk.getSdkResourceProvider()).isSameAs(resourceProvider);
  }

  @Test
  void getSdkResourceProvider_equivalentAutoWrappedResources_returnsFirst() {
    // Distinct RP instances resolving to the same Resource: composite check is equal-by-Resource
    // so this must succeed.
    ExtendedOpenTelemetrySdk sdk =
        buildExtended(RESOURCE_A, null, RESOURCE_A, null, RESOURCE_A, null);

    assertThat(sdk.getSdkResourceProvider().getResource()).isEqualTo(RESOURCE_A);
  }

  @Test
  void getSdkResourceProvider_mismatchedResources_throws() {
    ExtendedOpenTelemetrySdk sdk =
        buildExtended(RESOURCE_A, null, RESOURCE_B, null, RESOURCE_A, null);

    assertThatThrownBy(sdk::getSdkResourceProvider)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("configured with different");
  }

  private static ExtendedOpenTelemetrySdk buildExtended(
      @Nullable Resource tracerResource,
      @Nullable SdkResourceProvider tracerRp,
      @Nullable Resource meterResource,
      @Nullable SdkResourceProvider meterRp,
      @Nullable Resource loggerResource,
      @Nullable SdkResourceProvider loggerRp) {
    SdkTracerProviderBuilder tracerBuilder = SdkTracerProvider.builder();
    if (tracerResource != null) {
      tracerBuilder.setResource(tracerResource);
    }
    if (tracerRp != null) {
      SdkTracerProviderUtil.setSdkResourceProvider(tracerBuilder, tracerRp);
    }
    SdkMeterProviderBuilder meterBuilder =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create());
    if (meterResource != null) {
      meterBuilder.setResource(meterResource);
    }
    if (meterRp != null) {
      SdkMeterProviderUtil.setSdkResourceProvider(meterBuilder, meterRp);
    }
    SdkLoggerProviderBuilder loggerBuilder = SdkLoggerProvider.builder();
    if (loggerResource != null) {
      loggerBuilder.setResource(loggerResource);
    }
    if (loggerRp != null) {
      SdkLoggerProviderUtil.setSdkResourceProvider(loggerBuilder, loggerRp);
    }
    return (ExtendedOpenTelemetrySdk)
        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerBuilder.build())
            .setMeterProvider(meterBuilder.build())
            .setLoggerProvider(loggerBuilder.build())
            .build();
  }
}
