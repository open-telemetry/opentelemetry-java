/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.common.internal.ScopeConfigurator;
import io.opentelemetry.sdk.common.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOnSamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerConfiguratorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalTracerMatcherAndConfigModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SamplerModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProviderModel;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import io.opentelemetry.sdk.trace.internal.TracerConfig;
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

  private final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          SpiHelper.create(TracerProviderFactoryTest.class.getClassLoader()));

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(TracerProviderAndAttributeLimits model, SdkTracerProvider expectedProvider) {
    List<Closeable> closeables = new ArrayList<>();
    cleanup.addCloseable(expectedProvider);

    SdkTracerProvider provider = TracerProviderFactory.getInstance().create(model, context).build();
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
            TracerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(), new TracerProviderModel()),
            SdkTracerProvider.builder().build()),
        Arguments.of(
            TracerProviderAndAttributeLimits.create(
                new AttributeLimitsModel(),
                new TracerProviderModel()
                    .withLimits(
                        new SpanLimitsModel()
                            .withAttributeCountLimit(1)
                            .withAttributeValueLengthLimit(2)
                            .withEventCountLimit(3)
                            .withLinkCountLimit(4)
                            .withEventAttributeCountLimit(5)
                            .withLinkAttributeCountLimit(6))
                    .withSampler(new SamplerModel().withAlwaysOn(new AlwaysOnSamplerModel()))
                    .withProcessors(
                        Collections.singletonList(
                            new SpanProcessorModel()
                                .withBatch(
                                    new BatchSpanProcessorModel()
                                        .withExporter(
                                            new SpanExporterModel()
                                                .withOtlpHttp(new OtlpHttpExporterModel())))))
                    .withTracerConfiguratorDevelopment(
                        new ExperimentalTracerConfiguratorModel()
                            .withDefaultConfig(
                                new ExperimentalTracerConfigModel().withDisabled(true))
                            .withTracers(
                                Collections.singletonList(
                                    new ExperimentalTracerMatcherAndConfigModel()
                                        .withName("foo")
                                        .withConfig(
                                            new ExperimentalTracerConfigModel()
                                                .withDisabled(false)))))),
            addTracerConfigurator(
                    SdkTracerProvider.builder(),
                    ScopeConfigurator.<TracerConfig>builder()
                        .setDefault(TracerConfig.disabled())
                        .addCondition(
                            ScopeConfiguratorBuilder.nameMatchesGlob("foo"), TracerConfig.enabled())
                        .build())
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
                    BatchSpanProcessor.builder(OtlpHttpSpanExporter.getDefault()).build())
                .build()));
  }

  private static SdkTracerProviderBuilder addTracerConfigurator(
      SdkTracerProviderBuilder builder, ScopeConfigurator<TracerConfig> tracerConfigurator) {
    SdkTracerProviderUtil.setTracerConfigurator(builder, tracerConfigurator);
    return builder;
  }
}
