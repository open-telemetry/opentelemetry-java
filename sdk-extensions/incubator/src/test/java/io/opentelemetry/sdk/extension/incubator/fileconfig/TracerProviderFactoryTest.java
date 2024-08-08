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
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TracerProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(TracerProviderFactoryTest.class.getClassLoader());

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(TracerProviderAndAttributeLimits model, SdkTracerProvider expectedProvider) {
    List<Closeable> closeables = new ArrayList<>();
    cleanup.addCloseable(expectedProvider);

    SdkTracerProvider provider =
        TracerProviderFactory.getInstance().create(model, spiHelper, closeables).build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(
            TracerProviderAndAttributeLimits.create(null, null),
            SdkTracerProvider.builder().build()),
        Arguments.of(
            TracerProviderAndAttributeLimits.create(new AttributeLimits(), new TracerProvider()),
            SdkTracerProvider.builder().build()),
        Arguments.of(
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
                                        .withExporter(new SpanExporter().withOtlp(new Otlp())))))),
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
                .build()));
  }
}
