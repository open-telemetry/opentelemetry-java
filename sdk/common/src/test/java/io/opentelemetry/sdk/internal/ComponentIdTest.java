/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import org.junit.jupiter.api.Test;

class ComponentIdTest {

  @Test
  void testStandardTypesUpToDate() {
    assertThat(StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_SPAN_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_SPAN_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_SPAN_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_JSON_SPAN_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues
                .OTLP_HTTP_JSON_SPAN_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_GRPC_LOG_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_LOG_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_LOG_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_LOG_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_JSON_LOG_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_JSON_LOG_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_GRPC_METRIC_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_METRIC_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_METRIC_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_METRIC_EXPORTER);
    assertThat(StandardComponentId.ExporterType.OTLP_HTTP_JSON_METRIC_EXPORTER.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues
                .OTLP_HTTP_JSON_METRIC_EXPORTER);
    // TODO: uncomment when released in new semconv version
    //    assertThat(StandardComponentId.ExporterType.ZIPKIN_HTTP_SPAN_EXPORTER.value)
    //        .isEqualTo(
    //
    // OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.ZIPKIN_HTTP_SPAN_EXPORTER);

    assertThat(StandardComponentId.SpanProcessorType.BATCH_SPAN_PROCESSOR.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.BATCHING_SPAN_PROCESSOR);
    assertThat(StandardComponentId.SpanProcessorType.SIMPLE_SPAN_PROCESSOR.value)
        .isEqualTo(
            OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.SIMPLE_SPAN_PROCESSOR);
  }
}
