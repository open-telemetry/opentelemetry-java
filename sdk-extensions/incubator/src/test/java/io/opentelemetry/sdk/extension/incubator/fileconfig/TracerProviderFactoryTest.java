/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOn;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class TracerProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(TracerProviderFactoryTest.class.getClassLoader());

  @Test
  void create_Null() {
    List<Closeable> closeables = new ArrayList<>();
    SdkTracerProvider expectedProvider = SdkTracerProvider.builder().build();
    cleanup.addCloseable(expectedProvider);

    SdkTracerProvider provider =
        TracerProviderFactory.getInstance().create(null, spiHelper, closeables).build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    SdkTracerProvider expectedProvider = SdkTracerProvider.builder().build();
    cleanup.addCloseable(expectedProvider);

    SdkTracerProvider provider =
        TracerProviderFactory.getInstance()
            .create(
                TracerProviderAndAttributeLimits.create(
                    new AttributeLimits(), new TracerProvider()),
                spiHelper,
                closeables)
            .build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  @Test
  void create_Configured() {
    List<Closeable> closeables = new ArrayList<>();
    SdkTracerProvider expectedProvider =
        SdkTracerProvider.builder()
            .setSpanLimits(
                SpanLimits.builder()
                    .setMaxNumberOfAttributes(1)
                    .setMaxAttributeValueLength(2)
                    .setMaxNumberOfEvents(3)
                    .setMaxNumberOfLinks(4)
                    .setMaxNumberOfAttributesPerEvent(5)
                    .setMaxNumberOfAttributesPerLink(6)
                    .build())
            .setSampler(alwaysOn())
            .addSpanProcessor(
                io.opentelemetry.sdk.trace.export.BatchSpanProcessor.builder(
                        OtlpGrpcSpanExporter.getDefault())
                    .build())
            .build();
    cleanup.addCloseable(expectedProvider);

    SdkTracerProvider provider =
        TracerProviderFactory.getInstance()
            .create(
                TracerProviderAndAttributeLimits.create(
                    new AttributeLimits(),
                    new TracerProvider()
                        .withLimits(
                            new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                    .SpanLimits()
                                .withAttributeCountLimit(1)
                                .withAttributeValueLengthLimit(2)
                                .withEventCountLimit(3)
                                .withLinkCountLimit(4)
                                .withEventAttributeCountLimit(5)
                                .withLinkAttributeCountLimit(6))
                        .withSampler(new Sampler().withAlwaysOn(new AlwaysOn()))
                        .withProcessors(
                            Collections.singletonList(
                                new SpanProcessor()
                                    .withBatch(
                                        new BatchSpanProcessor()
                                            .withExporter(
                                                new SpanExporter().withOtlp(new Otlp())))))),
                spiHelper,
                closeables)
            .build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }
}
