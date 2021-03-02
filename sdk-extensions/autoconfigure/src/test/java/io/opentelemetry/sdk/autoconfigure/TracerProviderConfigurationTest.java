/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

// NB: We use AssertJ extracting to reflectively access implementation details to test configuration
// because the use of BatchSpanProcessor makes it difficult to verify values through public means.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TracerProviderConfigurationTest {

  private static final ConfigProperties EMPTY =
      ConfigProperties.createForTest(Collections.emptyMap());

  @Mock private SpanExporter exporter;

  @BeforeEach
  void setUp() {
    when(exporter.shutdown()).thenReturn(CompletableResultCode.ofSuccess());
  }

  @Test
  void configureTracerProvider() {
    Map<String, String> properties = new HashMap<>();
    properties.put("otel.bsp.schedule.delay", "100000");
    properties.put("otel.traces.sampler", "always_off");
    properties.put("otel.traces.exporter", "none");

    Resource resource = Resource.create(Attributes.builder().put("cat", "meow").build());
    // We don't have any exporters on classpath for this test so check no-op case. Exporter cases
    // are verified in other test sets like testFullConfig.
    SdkTracerProvider tracerProvider =
        TracerProviderConfiguration.configureTracerProvider(
            resource, ConfigProperties.createForTest(properties));
    try {
      assertThat(tracerProvider.getSampler()).isEqualTo(Sampler.alwaysOff());

      assertThat(tracerProvider)
          .extracting("sharedState")
          .satisfies(
              sharedState -> {
                assertThat(sharedState).extracting("resource").isEqualTo(resource);
                assertThat(sharedState)
                    .extracting("activeSpanProcessor")
                    .isEqualTo(SpanProcessor.composite());
              });
    } finally {
      tracerProvider.shutdown();
    }
  }

  @Test
  void configureSpanProcessor_empty() {
    BatchSpanProcessor processor =
        TracerProviderConfiguration.configureSpanProcessor(EMPTY, exporter);

    try {
      assertThat(processor)
          .extracting("worker")
          .satisfies(
              worker -> {
                assertThat(worker)
                    .extracting("scheduleDelayNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(5000));
                assertThat(worker)
                    .extracting("exporterTimeoutNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(30000));
                assertThat(worker).extracting("maxExportBatchSize").isEqualTo(512);
                assertThat(worker)
                    .extracting("queue")
                    .isInstanceOfSatisfying(
                        ArrayBlockingQueue.class,
                        queue -> assertThat(queue.remainingCapacity()).isEqualTo(2048));
                assertThat(worker).extracting("spanExporter").isEqualTo(exporter);
              });
    } finally {
      processor.shutdown();
    }
  }

  @Test
  void configureSpanProcessor_configured() {
    Map<String, String> properties = new HashMap<>();
    properties.put("otel.bsp.schedule.delay", "100000");
    properties.put("otel.bsp.max.queue.size", "2");
    properties.put("otel.bsp.max.export.batch.size", "3");
    properties.put("otel.bsp.export.timeout", "4");

    BatchSpanProcessor processor =
        TracerProviderConfiguration.configureSpanProcessor(
            ConfigProperties.createForTest(properties), exporter);

    try {
      assertThat(processor)
          .extracting("worker")
          .satisfies(
              worker -> {
                assertThat(worker)
                    .extracting("scheduleDelayNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(100000));
                assertThat(worker)
                    .extracting("exporterTimeoutNanos")
                    .isEqualTo(TimeUnit.MILLISECONDS.toNanos(4));
                assertThat(worker).extracting("maxExportBatchSize").isEqualTo(3);
                assertThat(worker)
                    .extracting("queue")
                    .isInstanceOfSatisfying(
                        ArrayBlockingQueue.class,
                        queue -> assertThat(queue.remainingCapacity()).isEqualTo(2));
                assertThat(worker).extracting("spanExporter").isEqualTo(exporter);
              });
    } finally {
      processor.shutdown();
    }
  }

  @Test
  void configureTraceConfig_empty() {
    assertThat(TracerProviderConfiguration.configureSpanLimits(EMPTY))
        .isEqualTo(SpanLimits.getDefault());
  }

  @Test
  void configureTraceConfig_full() {

    Map<String, String> properties = new HashMap<>();
    properties.put("otel.traces.sampler", "always_off");
    properties.put("otel.span.attribute.count.limit", "5");
    properties.put("otel.span.event.count.limit", "4");
    properties.put("otel.span.link.count.limit", "3");

    SpanLimits config =
        TracerProviderConfiguration.configureSpanLimits(ConfigProperties.createForTest(properties));
    assertThat(config.getMaxNumberOfAttributes()).isEqualTo(5);
    assertThat(config.getMaxNumberOfEvents()).isEqualTo(4);
    assertThat(config.getMaxNumberOfLinks()).isEqualTo(3);
  }

  @Test
  void configureSampler() {
    assertThat(TracerProviderConfiguration.configureSampler("always_on", EMPTY))
        .isEqualTo(Sampler.alwaysOn());
    assertThat(TracerProviderConfiguration.configureSampler("always_off", EMPTY))
        .isEqualTo(Sampler.alwaysOff());
    assertThat(
            TracerProviderConfiguration.configureSampler(
                "traceidratio",
                ConfigProperties.createForTest(
                    Collections.singletonMap("otel.traces.sampler.arg", "0.5"))))
        .isEqualTo(Sampler.traceIdRatioBased(0.5));
    assertThat(TracerProviderConfiguration.configureSampler("traceidratio", EMPTY))
        .isEqualTo(Sampler.traceIdRatioBased(1.0d));
    assertThat(TracerProviderConfiguration.configureSampler("parentbased_always_on", EMPTY))
        .isEqualTo(Sampler.parentBased(Sampler.alwaysOn()));
    assertThat(TracerProviderConfiguration.configureSampler("parentbased_always_off", EMPTY))
        .isEqualTo(Sampler.parentBased(Sampler.alwaysOff()));
    assertThat(
            TracerProviderConfiguration.configureSampler(
                "parentbased_traceidratio",
                ConfigProperties.createForTest(
                    Collections.singletonMap("otel.traces.sampler.arg", "0.4"))))
        .isEqualTo(Sampler.parentBased(Sampler.traceIdRatioBased(0.4)));
    assertThat(TracerProviderConfiguration.configureSampler("parentbased_traceidratio", EMPTY))
        .isEqualTo(Sampler.parentBased(Sampler.traceIdRatioBased(1.0d)));

    assertThatThrownBy(() -> TracerProviderConfiguration.configureSampler("catsampler", EMPTY))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unrecognized value for otel.traces.sampler: catsampler");
  }
}
