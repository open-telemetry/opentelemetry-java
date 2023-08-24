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
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpMetric.DefaultHistogramAggregation;
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

class ConfigurationReaderTest {

  @Test
  void read_KitchenSinkExampleFile() throws IOException {
    OpenTelemetryConfiguration expected = new OpenTelemetryConfiguration();

    expected.withFileFormat("0.1");
    expected.withDisabled(false);

    // General config
    Resource resource =
        new Resource().withAttributes(new Attributes().withServiceName("unknown_service"));
    expected.withResource(resource);

    AttributeLimits attributeLimits =
        new AttributeLimits().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    expected.withAttributeLimits(attributeLimits);

    List<String> propagators =
        Arrays.asList("tracecontext", "baggage", "b3", "b3multi", "jaeger", "xray", "ottrace");
    expected.withPropagators(propagators);

    // TracerProvider config
    TracerProvider tracerProvider = new TracerProvider();

    SpanLimits spanLimits =
        new SpanLimits()
            .withAttributeValueLengthLimit(4096)
            .withAttributeCountLimit(128)
            .withEventCountLimit(128)
            .withLinkCountLimit(128)
            .withEventAttributeCountLimit(128)
            .withLinkAttributeCountLimit(128);
    tracerProvider.withLimits(spanLimits);

    Sampler sampler =
        new Sampler()
            .withParentBased(
                new ParentBased()
                    .withRoot(
                        new Sampler()
                            .withTraceIdRatioBased(new TraceIdRatioBased().withRatio(0.0001)))
                    .withRemoteParentSampled(new Sampler().withAlwaysOn(new AlwaysOn()))
                    .withRemoteParentNotSampled(new Sampler().withAlwaysOff(new AlwaysOff()))
                    .withLocalParentSampled(new Sampler().withAlwaysOn(new AlwaysOn()))
                    .withLocalParentNotSampled(new Sampler().withAlwaysOff(new AlwaysOff())));
    tracerProvider.withSampler(sampler);

    SpanProcessor spanProcessor1 =
        new SpanProcessor()
            .withBatch(
                new BatchSpanProcessor()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new SpanExporter()
                            .withOtlp(
                                new Otlp()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    SpanProcessor spanProcessor2 =
        new SpanProcessor()
            .withSimple(
                new SimpleSpanProcessor()
                    .withExporter(new SpanExporter().withConsole(new Console())));
    tracerProvider.withProcessors(Arrays.asList(spanProcessor1, spanProcessor2));

    expected.withTracerProvider(tracerProvider);
    // end TracerProvider config

    // LoggerProvider config
    LoggerProvider loggerProvider = new LoggerProvider();

    LogRecordLimits logRecordLimits =
        new LogRecordLimits().withAttributeValueLengthLimit(4096).withAttributeCountLimit(128);
    loggerProvider.withLimits(logRecordLimits);

    LogRecordProcessor logRecordProcessor =
        new LogRecordProcessor()
            .withBatch(
                new BatchLogRecordProcessor()
                    .withScheduleDelay(5_000)
                    .withExportTimeout(30_000)
                    .withMaxQueueSize(2048)
                    .withMaxExportBatchSize(512)
                    .withExporter(
                        new LogRecordExporter()
                            .withOtlp(
                                new Otlp()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000))));
    loggerProvider.withProcessors(Collections.singletonList(logRecordProcessor));

    expected.withLoggerProvider(loggerProvider);
    // end LoggerProvider config

    // MeterProvider config
    MeterProvider meterProvider = new MeterProvider();

    MetricReader metricReader1 =
        new MetricReader()
            .withPull(
                new PullMetricReader()
                    .withExporter(
                        new MetricExporter()
                            .withPrometheus(
                                new Prometheus().withHost("localhost").withPort(9464))));
    MetricReader metricReader2 =
        new MetricReader()
            .withPeriodic(
                new PeriodicMetricReader()
                    .withInterval(5_000)
                    .withTimeout(30_000)
                    .withExporter(
                        new MetricExporter()
                            .withOtlp(
                                new OtlpMetric()
                                    .withProtocol("http/protobuf")
                                    .withEndpoint("http://localhost:4318")
                                    .withCertificate("/app/cert.pem")
                                    .withClientKey("/app/cert.pem")
                                    .withClientCertificate("/app/cert.pem")
                                    .withHeaders(
                                        new Headers().withAdditionalProperty("api-key", "1234"))
                                    .withCompression("gzip")
                                    .withTimeout(10_000)
                                    .withTemporalityPreference("delta")
                                    .withDefaultHistogramAggregation(
                                        DefaultHistogramAggregation
                                            .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM))));
    MetricReader metricReader3 =
        new MetricReader()
            .withPeriodic(
                new PeriodicMetricReader()
                    .withExporter(new MetricExporter().withConsole(new Console())));
    meterProvider.withReaders(Arrays.asList(metricReader1, metricReader2, metricReader3));

    View view =
        new View()
            .withSelector(
                new Selector()
                    .withInstrumentName("my-instrument")
                    .withInstrumentType(Selector.InstrumentType.HISTOGRAM)
                    .withUnit("ms")
                    .withMeterName("my-meter")
                    .withMeterVersion("1.0.0")
                    .withMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0"))
            .withStream(
                new Stream()
                    .withName("new_instrument_name")
                    .withDescription("new_description")
                    .withAggregation(
                        new Aggregation()
                            .withExplicitBucketHistogram(
                                new ExplicitBucketHistogram()
                                    .withBoundaries(
                                        Arrays.asList(
                                            0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0,
                                            750.0, 1000.0, 2500.0, 5000.0, 7500.0, 10000.0))
                                    .withRecordMinMax(true)))
                    .withAttributeKeys(Arrays.asList("key1", "key2")));
    meterProvider.withViews(Collections.singletonList(view));

    expected.withMeterProvider(meterProvider);
    // end MeterProvider config

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_DIR") + "/kitchen-sink.yaml")) {
      OpenTelemetryConfiguration config = ConfigurationReader.parse(configExampleFile);

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
      assertThat(config).isEqualTo(expected);
    }
  }
}
