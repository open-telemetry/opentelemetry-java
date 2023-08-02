/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Aggregation;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOff;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AlwaysOn;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Console;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExplicitBucketHistogram;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Headers;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ParentBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PeriodicMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Prometheus;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.PullMetricReader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Resource;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Sampler;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Selector;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SimpleSpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Stream;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TraceIdRatioBased;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.View;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class ConfigurationReaderTest {

  @Test
  void read_KitchenSinkExampleFile() throws IOException {
    OpentelemetryConfiguration.OpentelemetryConfigurationBuilder expectedBuilder =
        new OpentelemetryConfiguration.OpentelemetryConfigurationBuilder();

    expectedBuilder.withFileFormat("0.1");

    // General config
    Resource resource =
        new Resource.ResourceBuilder()
            .withAttributes(
                new Attributes.AttributesBuilder().withServiceName("unknown_service").build())
            .build();
    expectedBuilder.withResource(resource);

    AttributeLimits attributeLimits =
        new AttributeLimits.AttributeLimitsBuilder()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .build();
    expectedBuilder.withAttributeLimits(attributeLimits);

    List<String> propagators =
        Arrays.asList("tracecontext", "baggage", "b3", "b3multi", "jaeger", "xray", "ottrace");
    expectedBuilder.withPropagators(propagators);

    // TracerProvider config
    TracerProvider.TracerProviderBuilder tracerProviderBuilder =
        new TracerProvider.TracerProviderBuilder();

    SpanLimits spanLimits =
        new SpanLimits.SpanLimitsBuilder()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .withEventCountLimit(128)
            .withLinkCountLimit(128)
            .withEventAttributeCountLimit(128)
            .withLinkAttributeCountLimit(128)
            .build();
    tracerProviderBuilder.withLimits(spanLimits);

    Sampler sampler =
        new Sampler.SamplerBuilder()
            .withParentBased(
                new ParentBased.ParentBasedBuilder()
                    .withRoot(
                        new Sampler.SamplerBuilder()
                            .withTraceIdRatioBased(
                                new TraceIdRatioBased.TraceIdRatioBasedBuilder()
                                    .withRatio(0.0001)
                                    .build())
                            .build())
                    .withRemoteParentSampled(
                        new Sampler.SamplerBuilder()
                            .withAlwaysOn(new AlwaysOn.AlwaysOnBuilder().build())
                            .build())
                    .withRemoteParentNotSampled(
                        new Sampler.SamplerBuilder()
                            .withAlwaysOff(new AlwaysOff.AlwaysOffBuilder().build())
                            .build())
                    .withLocalParentSampled(
                        new Sampler.SamplerBuilder()
                            .withAlwaysOn(new AlwaysOn.AlwaysOnBuilder().build())
                            .build())
                    .withLocalParentNotSampled(
                        new Sampler.SamplerBuilder()
                            .withAlwaysOff(new AlwaysOff.AlwaysOffBuilder().build())
                            .build())
                    .build())
            .build();
    tracerProviderBuilder.withSampler(sampler);

    SpanProcessor spanProcessor1 =
        new SpanProcessor.SpanProcessorBuilder()
            .withBatch(
                new BatchSpanProcessor.BatchSpanProcessorBuilder()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new SpanExporter.SpanExporterBuilder()
                            .withOtlp(
                                new Otlp.OtlpBuilder()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers.HeadersBuilder()
                                            .withAdditionalProperty("api-key", "1234")
                                            .build())
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .build())
                            .build())
                    .build())
            .build();
    SpanProcessor spanProcessor2 =
        new SpanProcessor.SpanProcessorBuilder()
            .withSimple(
                new SimpleSpanProcessor.SimpleSpanProcessorBuilder()
                    .withExporter(
                        new SpanExporter.SpanExporterBuilder()
                            .withConsole(new Console.ConsoleBuilder().build())
                            .build())
                    .build())
            .build();
    tracerProviderBuilder.withProcessors(Arrays.asList(spanProcessor1, spanProcessor2));

    expectedBuilder.withTracerProvider(tracerProviderBuilder.build());
    // end TracerProvider config

    // LoggerProvider config
    LoggerProvider.LoggerProviderBuilderBase<?> loggerProviderBuilder =
        new LoggerProvider.LoggerProviderBuilder();

    LogRecordLimits logRecordLimits =
        new LogRecordLimits.LogRecordLimitsBuilder()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .build();
    loggerProviderBuilder.withLimits(logRecordLimits);

    LogRecordProcessor logRecordProcessor =
        new LogRecordProcessor.LogRecordProcessorBuilder()
            .withBatch(
                new BatchLogRecordProcessor.BatchLogRecordProcessorBuilder()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new LogRecordExporter.LogRecordExporterBuilder()
                            .withOtlp(
                                new Otlp.OtlpBuilder()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers.HeadersBuilder()
                                            .withAdditionalProperty("api-key", "1234")
                                            .build())
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .build())
                            .build())
                    .build())
            .build();
    loggerProviderBuilder.withProcessors(Collections.singletonList(logRecordProcessor));

    expectedBuilder.withLoggerProvider(loggerProviderBuilder.build());
    // end LoggerProvider config

    // MeterProvider config
    MeterProvider.MeterProviderBuilderBase<?> meterProviderBuilder =
        new MeterProvider.MeterProviderBuilder();

    MetricReader metricReader1 =
        new MetricReader.MetricReaderBuilder()
            .withPull(
                new PullMetricReader.PullMetricReaderBuilder()
                    .withExporter(
                        new MetricExporter.MetricExporterBuilder()
                            .withPrometheus(
                                new Prometheus.PrometheusBuilder()
                                    .withHost("localhost")
                                    .withPort(9464)
                                    .build())
                            .build())
                    .build())
            .build();
    MetricReader metricReader2 =
        new MetricReader.MetricReaderBuilder()
            .withPeriodic(
                new PeriodicMetricReader.PeriodicMetricReaderBuilder()
                    .withInterval(5_000)
                    .withTimeout(30_000)
                    .withExporter(
                        new MetricExporter.MetricExporterBuilder()
                            .withOtlp(
                                new OtlpMetric.OtlpMetricBuilder()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers.HeadersBuilder()
                                            .withAdditionalProperty("api-key", "1234")
                                            .build())
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withTemporalityPreference("delta")
                                    .withDefaultHistogramAggregation("exponential_bucket_histogram")
                                    .build())
                            .build())
                    .build())
            .build();
    MetricReader metricReader3 =
        new MetricReader.MetricReaderBuilder()
            .withPeriodic(
                new PeriodicMetricReader.PeriodicMetricReaderBuilder()
                    .withExporter(
                        new MetricExporter.MetricExporterBuilder()
                            .withConsole(new Console.ConsoleBuilder().build())
                            .build())
                    .build())
            .build();
    meterProviderBuilder.withReaders(Arrays.asList(metricReader1, metricReader2, metricReader3));

    View view =
        new View.ViewBuilder()
            .withSelector(
                new Selector.SelectorBuilder()
                    .withInstrumentName("my-instrument")
                    .withInstrumentType("histogram")
                    .withMeterName("my-meter")
                    .withMeterVersion("1.0.0")
                    .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0")
                    .build())
            .withStream(
                new Stream.StreamBuilder()
                    .withName("new_instrument_name")
                    .withDescription("new_description")
                    .withAggregation(
                        new Aggregation.AggregationBuilder()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogram.ExplicitBucketHistogramBuilder()
                                    .withBoundaries(
                                        Arrays.asList(
                                            0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0,
                                            750.0, 1000.0, 2500.0, 5000.0, 7500.0, 10000.0))
                                    .withRecordMinMax(true)
                                    .build())
                            .build())
                    .withAttributeKeys(Arrays.asList("key1", "key2"))
                    .build())
            .build();
    meterProviderBuilder.withViews(Collections.singletonList(view));

    expectedBuilder.withMeterProvider(meterProviderBuilder.build());
    // end MeterProvider config

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_DIR") + "/kitchen-sink.yaml")) {
      OpentelemetryConfiguration config = ConfigurationReader.parse(configExampleFile);

      // General config
      assertThat(config.getFileFormat()).isEqualTo("0.1");
      assertThat(config.getResource()).isEqualTo(resource);
      assertThat(config.getAttributeLimits()).isEqualTo(attributeLimits);
      assertThat(config.getPropagators()).isEqualTo(propagators);

      // TracerProvider config
      TracerProvider configTracerProvider = config.getTracerProvider();
      assertThat(configTracerProvider.getLimits()).isEqualTo(spanLimits);
      assertThat(configTracerProvider.getSampler()).isEqualTo(sampler);
      assertThat(configTracerProvider.getProcessors())
          .isEqualTo(Arrays.asList(spanProcessor1, spanProcessor2));

      // LoggerProvider config
      LoggerProvider configLoggerProvider = config.getLoggerProvider();
      assertThat(configLoggerProvider.getLimits()).isEqualTo(logRecordLimits);
      assertThat(configLoggerProvider.getProcessors())
          .isEqualTo(Collections.singletonList(logRecordProcessor));

      // MeterProvider config
      MeterProvider configMeterProvider = config.getMeterProvider();
      assertThat(configMeterProvider.getReaders())
          .isEqualTo(Arrays.asList(metricReader1, metricReader2, metricReader3));
      assertThat(configMeterProvider.getViews()).isEqualTo(Collections.singletonList(view));

      // All configuration
      assertThat(config).isEqualTo(expectedBuilder.build());
    }
  }
}
