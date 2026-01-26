/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.internal.all.OpenTelemetrySdkBuilderUtil;
import io.opentelemetry.sdk.internal.all.SdkConfigProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DeclarativeConfigurationSpiTest {

  @RegisterExtension private static final CleanupExtension cleanup = new CleanupExtension();

  @Test
  void configFromSpi() {
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
                OpenTelemetrySdk.builder()
                    .setTracerProvider(
                        SdkTracerProvider.builder()
                            .setResource(
                                Resource.getDefault().toBuilder()
                                    .put("service.name", "test")
                                    .build())
                            .addSpanProcessor(
                                SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                            .build()),
                SdkConfigProvider.create(DeclarativeConfigProperties.empty()))
            .build();
    cleanup.addCloseable(expectedSdk);
    AutoConfiguredOpenTelemetrySdkBuilder builder = spy(AutoConfiguredOpenTelemetrySdk.builder());
    Thread thread = new Thread();
    doReturn(thread).when(builder).shutdownHook(any());

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk = builder.build();
    cleanup.addCloseable(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());

    assertThat(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().toString())
        .isEqualTo(expectedSdk.toString());

    assertThat(TestResourceDetector.initialized).isTrue();
  }
}
