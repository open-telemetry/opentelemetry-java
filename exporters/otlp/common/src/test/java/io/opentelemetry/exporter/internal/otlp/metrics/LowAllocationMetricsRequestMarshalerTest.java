/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

class LowAllocationMetricsRequestMarshalerTest {

  @ParameterizedTest
  @ArgumentsSource(MetricsProvider.class)
  void validateOutput(Collection<MetricData> metrics) throws Exception {
    byte[] result;
    {
      MetricsRequestMarshaler requestMarshaler = MetricsRequestMarshaler.create(metrics);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      LowAllocationMetricsRequestMarshaler requestMarshaler =
          new LowAllocationMetricsRequestMarshaler();
      requestMarshaler.initialize(metrics);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @ParameterizedTest
  @ArgumentsSource(MetricsProvider.class)
  void validateJsonOutput(Collection<MetricData> metrics) throws Exception {
    String result;
    {
      MetricsRequestMarshaler requestMarshaler = MetricsRequestMarshaler.create(metrics);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      result = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    String lowAllocationResult;
    {
      LowAllocationMetricsRequestMarshaler requestMarshaler =
          new LowAllocationMetricsRequestMarshaler();
      requestMarshaler.initialize(metrics);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(requestMarshaler.getBinarySerializedSize());
      requestMarshaler.writeJsonTo(customOutput);
      lowAllocationResult = new String(customOutput.toByteArray(), StandardCharsets.UTF_8);
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @ParameterizedTest
  @ArgumentsSource(ExemplarProvider.class)
  void validateExemplar(ExemplarData exemplar) throws Exception {
    byte[] result;
    {
      Marshaler marshaler = ExemplarMarshaler.create(exemplar);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(marshaler.getBinarySerializedSize());
      marshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      MarshalerContext context = new MarshalerContext();
      class TestMarshaler extends MarshalerWithSize {

        TestMarshaler() {
          super(ExemplarStatelessMarshaler.INSTANCE.getBinarySerializedSize(exemplar, context));
        }

        @Override
        protected void writeTo(Serializer output) throws IOException {
          ExemplarStatelessMarshaler.INSTANCE.writeTo(output, exemplar, context);
        }
      }
      Marshaler marshaler = new TestMarshaler();
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(marshaler.getBinarySerializedSize());
      marshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  @Test
  void validateSummary() throws Exception {
    List<ValueAtQuantile> percentileValues =
        Arrays.asList(ImmutableValueAtQuantile.create(3.0, 4.0));
    List<SummaryPointData> points =
        Arrays.asList(
            ImmutableSummaryPointData.create(
                12345, 12346, Attributes.empty(), 1, 2.0, percentileValues));
    SummaryData summary = ImmutableSummaryData.create(points);

    byte[] result;
    {
      Marshaler marshaler = SummaryMarshaler.create(summary);
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(marshaler.getBinarySerializedSize());
      marshaler.writeBinaryTo(customOutput);
      result = customOutput.toByteArray();
    }

    byte[] lowAllocationResult;
    {
      MarshalerContext context = new MarshalerContext();
      class TestMarshaler extends MarshalerWithSize {

        TestMarshaler() {
          super(SummaryStatelessMarshaler.INSTANCE.getBinarySerializedSize(summary, context));
        }

        @Override
        protected void writeTo(Serializer output) throws IOException {
          SummaryStatelessMarshaler.INSTANCE.writeTo(output, summary, context);
        }
      }
      Marshaler marshaler = new TestMarshaler();
      ByteArrayOutputStream customOutput =
          new ByteArrayOutputStream(marshaler.getBinarySerializedSize());
      marshaler.writeBinaryTo(customOutput);
      lowAllocationResult = customOutput.toByteArray();
    }

    assertThat(lowAllocationResult).isEqualTo(result);
  }

  private static Collection<MetricData> metrics(Consumer<MeterProvider> metricProducer) {
    InMemoryMetricReader metricReader = InMemoryMetricReader.create();
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(metricReader)
            .registerView(
                InstrumentSelector.builder().setName("exponentialhistogram").build(),
                View.builder()
                    .setAggregation(Aggregation.base2ExponentialBucketHistogram())
                    .build())
            .setResource(
                Resource.create(
                    Attributes.builder()
                        .put(AttributeKey.booleanKey("key_bool"), true)
                        .put(AttributeKey.stringKey("key_string"), "string")
                        .put(AttributeKey.longKey("key_int"), 100L)
                        .put(AttributeKey.doubleKey("key_double"), 100.3)
                        .put(
                            AttributeKey.stringArrayKey("key_string_array"),
                            Arrays.asList("string", "string"))
                        .put(AttributeKey.longArrayKey("key_long_array"), Arrays.asList(12L, 23L))
                        .put(
                            AttributeKey.doubleArrayKey("key_double_array"),
                            Arrays.asList(12.3, 23.1))
                        .put(
                            AttributeKey.booleanArrayKey("key_boolean_array"),
                            Arrays.asList(true, false))
                        .build()))
            .build();
    metricProducer.accept(meterProvider);

    return metricReader.collectAllMetrics();
  }

  private static class MetricsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      return Stream.of(
          arguments(
              named(
                  "long gauge",
                  metrics(
                      meterProvider ->
                          meterProvider
                              .get("long gauge")
                              .gaugeBuilder("gauge")
                              .setDescription("gauge description")
                              .setUnit("unit")
                              .ofLongs()
                              .buildWithCallback(
                                  measurement ->
                                      measurement.record(
                                          5,
                                          Attributes.of(
                                              AttributeKey.stringKey("key"), "value")))))),
          arguments(
              named(
                  "long counter",
                  metrics(
                      meterProvider -> {
                        LongCounter longCounter =
                            meterProvider
                                .get("long counter")
                                .counterBuilder("counter")
                                .setDescription("counter description")
                                .setUnit("unit")
                                .build();
                        longCounter.add(1);
                        longCounter.add(2, Attributes.of(AttributeKey.longKey("lives"), 9L));
                        longCounter.add(3);
                      }))),
          arguments(
              named(
                  "long updowncounter",
                  metrics(
                      meterProvider -> {
                        LongUpDownCounter longUpDownCounter =
                            meterProvider
                                .get("long updowncounter")
                                .upDownCounterBuilder("updowncounter")
                                .setDescription("updowncounter description")
                                .setUnit("unit")
                                .build();
                        longUpDownCounter.add(1);
                        longUpDownCounter.add(
                            -1, Attributes.of(AttributeKey.booleanKey("on"), true));
                        longUpDownCounter.add(1);
                      }))),
          arguments(
              named(
                  "double gauge",
                  metrics(
                      meterProvider ->
                          meterProvider
                              .get("double gauge")
                              .gaugeBuilder("doublegauge")
                              .setDescription("doublegauge")
                              .setUnit("unit")
                              .buildWithCallback(measurement -> measurement.record(5.0))))),
          arguments(
              named(
                  "double counter",
                  metrics(
                      meterProvider -> {
                        DoubleCounter doubleCounter =
                            meterProvider
                                .get("double counter")
                                .counterBuilder("doublecounter")
                                .ofDoubles()
                                .build();
                        doubleCounter.add(1.0);
                        doubleCounter.add(2.0);
                      }))),
          arguments(
              named(
                  "double updowncounter",
                  metrics(
                      meterProvider -> {
                        DoubleUpDownCounter doubleUpDownCounter =
                            meterProvider
                                .get("double updowncounter")
                                .upDownCounterBuilder("doubleupdown")
                                .ofDoubles()
                                .build();
                        doubleUpDownCounter.add(1.0);
                        doubleUpDownCounter.add(-1.0);
                      }))),
          arguments(
              named(
                  "double histogram",
                  metrics(
                      meterProvider -> {
                        DoubleHistogram histogram =
                            meterProvider
                                .get("double histogram")
                                .histogramBuilder("histogram")
                                .build();
                        histogram.record(1.0);
                        histogram.record(2.0);
                        histogram.record(3.0);
                        histogram.record(4.0);
                        histogram.record(5.0);
                      }))),
          arguments(
              named(
                  "long histogram",
                  metrics(
                      meterProvider -> {
                        LongHistogram histogram =
                            meterProvider
                                .get("long histogram")
                                .histogramBuilder("histogram")
                                .ofLongs()
                                .build();
                        histogram.record(1);
                        histogram.record(2);
                        histogram.record(3);
                        histogram.record(4);
                        histogram.record(5);
                      }))),
          arguments(
              named(
                  "double exponential histogram",
                  metrics(
                      meterProvider -> {
                        DoubleHistogram histogram =
                            meterProvider
                                .get("double exponential histogram")
                                .histogramBuilder("exponentialhistogram")
                                .build();
                        histogram.record(1.0);
                        histogram.record(2.0);
                        histogram.record(3.0);
                        histogram.record(4.0);
                        histogram.record(5.0);
                      }))),
          arguments(
              named(
                  "long exponential histogram",
                  metrics(
                      meterProvider -> {
                        DoubleHistogram histogram =
                            meterProvider
                                .get("long exponential histogram")
                                .histogramBuilder("exponentialhistogram")
                                .build();
                        histogram.record(1);
                        histogram.record(2);
                        histogram.record(3);
                        histogram.record(4);
                        histogram.record(5);
                      }))));
    }
  }

  private static class ExemplarProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(
        ParameterDeclarations parameters, ExtensionContext context) {
      SpanContext spanContext =
          SpanContext.create(
              "7b2e170db4df2d593ddb4ddf2ddf2d59",
              "170d3ddb4d23e81f",
              TraceFlags.getSampled(),
              TraceState.getDefault());

      return Stream.of(
          arguments(
              named(
                  "double exemplar",
                  ImmutableDoubleExemplarData.create(Attributes.empty(), 12345, spanContext, 5.0))),
          arguments(
              named(
                  "long exemplar",
                  ImmutableLongExemplarData.create(Attributes.empty(), 12345, spanContext, 5))));
    }
  }
}
