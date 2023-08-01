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
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class ConfigurationReaderTest {

  @Test
  void read_KitchenSinkExampleFile() throws IOException {
    OpentelemetryConfiguration expected = new OpentelemetryConfiguration();

    expected.setFileFormat("0.1");

    // General config
    Resource resource = new Resource();
    expected.setResource(resource);
    Attributes attributes = new Attributes();
    resource.setAttributes(attributes);
    attributes.setServiceName("unknown_service");

    AttributeLimits attributeLimits = new AttributeLimits();
    expected.setAttributeLimits(attributeLimits);
    attributeLimits.setAttributeValueLengthLimit(4096);
    attributeLimits.setAttributeCountLimit(128);

    List<String> propagators =
        Arrays.asList("tracecontext", "baggage", "b3", "b3multi", "jaeger", "xray", "ottrace");
    expected.setPropagators(propagators);

    // TracerProvider config
    TracerProvider tracerProvider = new TracerProvider();
    expected.setTracerProvider(tracerProvider);

    SpanLimits spanLimits = new SpanLimits();
    tracerProvider.setLimits(spanLimits);
    spanLimits.setAttributeValueLengthLimit(4096);
    spanLimits.setAttributeCountLimit(128);
    spanLimits.setEventCountLimit(128);
    spanLimits.setLinkCountLimit(128);
    spanLimits.setEventAttributeCountLimit(128);
    spanLimits.setLinkAttributeCountLimit(128);

    Sampler sampler =
        createSampler(
            it -> {
              ParentBased parentBased = new ParentBased();
              it.setParentBased(parentBased);
              parentBased.setRoot(
                  createSampler(
                      it1 -> {
                        TraceIdRatioBased traceIdRatioBased = new TraceIdRatioBased();
                        it1.setTraceIdRatioBased(traceIdRatioBased);
                        traceIdRatioBased.setRatio(0.0001);
                      }));
              parentBased.setRemoteParentSampled(
                  createSampler(it1 -> it1.setAlwaysOn(new AlwaysOn())));
              parentBased.setRemoteParentNotSampled(
                  createSampler(it1 -> it1.setAlwaysOff(new AlwaysOff())));
              parentBased.setLocalParentSampled(
                  createSampler(it1 -> it1.setAlwaysOn(new AlwaysOn())));
              parentBased.setLocalParentNotSampled(
                  createSampler(it1 -> it1.setAlwaysOff(new AlwaysOff())));
            });
    tracerProvider.setSampler(sampler);

    SpanProcessor spanProcessor1 = new SpanProcessor();
    BatchSpanProcessor batchSpanProcessor = new BatchSpanProcessor();
    spanProcessor1.setBatch(batchSpanProcessor);
    batchSpanProcessor.setScheduleDelay(5000);
    batchSpanProcessor.setExportTimeout(30_000);
    batchSpanProcessor.setMaxQueueSize(2048);
    batchSpanProcessor.setMaxExportBatchSize(512);
    SpanExporter spanExporter1 = new SpanExporter();
    batchSpanProcessor.setExporter(spanExporter1);
    Otlp otlpExporter = new Otlp();
    spanExporter1.setOtlp(otlpExporter);
    otlpExporter.setProtocol("http/protobuf");
    otlpExporter.setEndpoint("http://localhost:4318");
    otlpExporter.setCertificate("/app/cert.pem");
    otlpExporter.setClientKey("/app/cert.pem");
    otlpExporter.setClientCertificate("/app/cert.pem");
    Headers headers = new Headers();
    otlpExporter.setHeaders(headers);
    headers.setAdditionalProperty("api-key", "1234");
    otlpExporter.setCompression("gzip");
    otlpExporter.setTimeout(10_000);

    SpanProcessor spanProcessor2 = new SpanProcessor();
    SimpleSpanProcessor simpleSpanProcessor = new SimpleSpanProcessor();
    spanProcessor2.setSimple(simpleSpanProcessor);
    SpanExporter spanExporter2 = new SpanExporter();
    Console consoleExporter = new Console();
    spanExporter2.setConsole(consoleExporter);
    simpleSpanProcessor.setExporter(spanExporter2);

    tracerProvider.setProcessors(Arrays.asList(spanProcessor1, spanProcessor2));
    // end TracerProvider config

    // LoggerProvider config
    LoggerProvider loggerProvider = new LoggerProvider();
    expected.setLoggerProvider(loggerProvider);

    LogRecordLimits logRecordLimits = new LogRecordLimits();
    loggerProvider.setLimits(logRecordLimits);
    logRecordLimits.setAttributeValueLengthLimit(4096);
    logRecordLimits.setAttributeCountLimit(128);

    LogRecordProcessor logRecordProcessor = new LogRecordProcessor();
    BatchLogRecordProcessor batchLogRecordProcessor = new BatchLogRecordProcessor();
    logRecordProcessor.setBatch(batchLogRecordProcessor);
    batchLogRecordProcessor.setScheduleDelay(5000);
    batchLogRecordProcessor.setExportTimeout(30_000);
    batchLogRecordProcessor.setMaxQueueSize(2048);
    batchLogRecordProcessor.setMaxExportBatchSize(512);
    LogRecordExporter logRecordExporter = new LogRecordExporter();
    batchLogRecordProcessor.setExporter(logRecordExporter);
    logRecordExporter.setOtlp(otlpExporter);

    loggerProvider.setProcessors(Collections.singletonList(logRecordProcessor));
    // end LoggerProvider config

    // MeterProvider config
    MeterProvider meterProvider = new MeterProvider();
    expected.setMeterProvider(meterProvider);

    MetricReader metricReader1 = new MetricReader();
    PullMetricReader pullMetricReader = new PullMetricReader();
    metricReader1.setPull(pullMetricReader);
    MetricExporter metricExporter1 = new MetricExporter();
    pullMetricReader.setExporter(metricExporter1);
    Prometheus prometheus = new Prometheus();
    metricExporter1.setPrometheus(prometheus);
    prometheus.setHost("localhost");
    prometheus.setPort(9464);

    MetricReader metricReader2 = new MetricReader();
    PeriodicMetricReader periodicMetricReader1 = new PeriodicMetricReader();
    metricReader2.setPeriodic(periodicMetricReader1);
    periodicMetricReader1.setInterval(5000);
    periodicMetricReader1.setTimeout(30_000);
    MetricExporter metricExporter2 = new MetricExporter();
    periodicMetricReader1.setExporter(metricExporter2);
    OtlpMetric otlpMetricExporter = new OtlpMetric();
    metricExporter2.setOtlp(otlpMetricExporter);
    otlpMetricExporter.setProtocol("http/protobuf");
    otlpMetricExporter.setEndpoint("http://localhost:4318");
    otlpMetricExporter.setCertificate("/app/cert.pem");
    otlpMetricExporter.setClientKey("/app/cert.pem");
    otlpMetricExporter.setClientCertificate("/app/cert.pem");
    Headers headers1 = new Headers();
    otlpMetricExporter.setHeaders(headers1);
    headers1.setAdditionalProperty("api-key", "1234");
    otlpMetricExporter.setCompression("gzip");
    otlpMetricExporter.setTimeout(10_000);
    otlpMetricExporter.setTemporalityPreference("delta");
    otlpMetricExporter.setDefaultHistogramAggregation("exponential_bucket_histogram");

    MetricReader metricReader3 = new MetricReader();
    PeriodicMetricReader periodicMetricReader2 = new PeriodicMetricReader();
    metricReader3.setPeriodic(periodicMetricReader2);
    MetricExporter metricExporter3 = new MetricExporter();
    periodicMetricReader2.setExporter(metricExporter3);
    metricExporter3.setConsole(new Console());

    meterProvider.setReaders(Arrays.asList(metricReader1, metricReader2, metricReader3));

    View view = new View();
    Selector selector = new Selector();
    view.setSelector(selector);
    selector.setInstrumentName("my-instrument");
    selector.setInstrumentType("histogram");
    selector.setMeterName("my-meter");
    selector.setMeterVersion("1.0.0");
    selector.setMeterSchemaUrl("https://opentelemetry.io/schemas/1.16.0");

    Stream stream = new Stream();
    view.setStream(stream);
    stream.setName("new_instrument_name");
    stream.setDescription("new_description");
    Aggregation aggregation = new Aggregation();
    stream.setAggregation(aggregation);
    ExplicitBucketHistogram explicitBucketHistogram = new ExplicitBucketHistogram();
    aggregation.setExplicitBucketHistogram(explicitBucketHistogram);
    explicitBucketHistogram.setBoundaries(
        Arrays.asList(
            0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 750.0, 1000.0, 2500.0, 5000.0,
            7500.0, 10000.0));
    explicitBucketHistogram.setRecordMinMax(true);
    stream.setAttributeKeys(Arrays.asList("key1", "key2"));

    meterProvider.setViews(Collections.singletonList(view));
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
      assertThat(config).isEqualTo(expected);
    }
  }

  private static Sampler createSampler(Consumer<Sampler> samplerConsumer) {
    Sampler sampler = new Sampler();
    samplerConsumer.accept(sampler);
    return sampler;
  }
}
