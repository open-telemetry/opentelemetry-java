package io.opentelemetry.sdk.internal;


import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComponentIdTest {

  @Test
  void testStandardTypesUpToDate() {
    assertThat(ComponentId.StandardExporterType.OTLP_GRPC_SPAN_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_SPAN_EXPORTER);
    assertThat(ComponentId.StandardExporterType.OTLP_HTTP_SPAN_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_SPAN_EXPORTER);
    assertThat(ComponentId.StandardExporterType.OTLP_HTTP_JSON_SPAN_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_JSON_SPAN_EXPORTER);
    assertThat(ComponentId.StandardExporterType.OTLP_GRPC_LOG_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_LOG_EXPORTER);
    assertThat(ComponentId.StandardExporterType.OTLP_HTTP_LOG_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_LOG_EXPORTER);
    assertThat(ComponentId.StandardExporterType.OTLP_HTTP_JSON_LOG_EXPORTER.toString())
        .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_JSON_LOG_EXPORTER);
    // TODO: uncomment as soon as available in semconv release
    // assertThat(ComponentId.StandardType.OTLP_GRPC_METRIC_EXPORTER.toString())
    //   .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_GRPC_METRIC_EXPORTER);
    // assertThat(ComponentId.StandardType.OTLP_HTTP_METRIC_EXPORTER.toString())
    //   .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_METRIC_EXPORTER);
    // assertThat(ComponentId.StandardType.OTLP_HTTP_JSON_METRIC_EXPORTER.toString())
    //   .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.OTLP_HTTP_JSON_METRIC_EXPORTER);
    // assertThat(ComponentId.StandardType.ZIPKIN_HTTP_SPAN_EXPORTER.toString())
    //   .isEqualTo(OtelIncubatingAttributes.OtelComponentTypeIncubatingValues.ZIPKIN_HTTP_SPAN_EXPORTER);
  }

}
