/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.semconv.ErrorAttributes;
import io.opentelemetry.semconv.HttpAttributes;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.incubating.OtelIncubatingAttributes;
import io.opentelemetry.semconv.incubating.OtelIncubatingMetrics;
import io.opentelemetry.semconv.incubating.RpcIncubatingAttributes;
import org.junit.jupiter.api.Test;

class SemConvConstantsTest {

  @Test
  void attributeKeys() {
    assertThat(SemConvConstants.OTEL_COMPONENT_NAME)
        .isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_NAME);
    assertThat(SemConvConstants.OTEL_COMPONENT_TYPE)
        .isEqualTo(OtelIncubatingAttributes.OTEL_COMPONENT_TYPE);

    assertThat(SemConvConstants.ERROR_TYPE).isEqualTo(ErrorAttributes.ERROR_TYPE);

    assertThat(SemConvConstants.SERVER_ADDRESS).isEqualTo(ServerAttributes.SERVER_ADDRESS);
    assertThat(SemConvConstants.SERVER_PORT).isEqualTo(ServerAttributes.SERVER_PORT);
    assertThat(SemConvConstants.RPC_RESPONSE_STATUS_CODE)
        .isEqualTo(RpcIncubatingAttributes.RPC_RESPONSE_STATUS_CODE);
    assertThat(SemConvConstants.HTTP_RESPONSE_STATUS_CODE)
        .isEqualTo(HttpAttributes.HTTP_RESPONSE_STATUS_CODE);

    assertThat(SemConvConstants.OTEL_SPAN_PARENT_ORIGIN)
        .isEqualTo(OtelIncubatingAttributes.OTEL_SPAN_PARENT_ORIGIN);
    assertThat(SemConvConstants.OTEL_SPAN_SAMPLING_RESULT)
        .isEqualTo(OtelIncubatingAttributes.OTEL_SPAN_SAMPLING_RESULT);
  }

  @Test
  void spanMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_SPAN_STARTED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_STARTED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_SPAN_STARTED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_STARTED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_SPAN_STARTED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_STARTED_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_SPAN_LIVE_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_LIVE_NAME);
    assertThat(SemConvConstants.OTEL_SDK_SPAN_LIVE_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_LIVE_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_SPAN_LIVE_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_SPAN_LIVE_DESCRIPTION);
  }

  @Test
  void spanProcessorMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_PROCESSED_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_DESCRIPTION);
  }

  @Test
  void logMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_LOG_CREATED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_LOG_CREATED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_LOG_CREATED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_LOG_CREATED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_LOG_CREATED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_LOG_CREATED_DESCRIPTION);
  }

  @Test
  void logProcessorMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_PROCESSED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_PROCESSED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_PROCESSED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_PROCESSED_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_NAME);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_DESCRIPTION);
  }

  @Test
  void metricReaderMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_NAME);
    assertThat(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_METRIC_READER_COLLECTION_DURATION_DESCRIPTION);
  }

  @Test
  void exporterMetrics() {
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_OPERATION_DURATION_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_OPERATION_DURATION_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_OPERATION_DURATION_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_OPERATION_DURATION_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_OPERATION_DURATION_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_OPERATION_DURATION_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_INFLIGHT_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_EXPORTED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_EXPORTED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_SPAN_EXPORTED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_SPAN_EXPORTED_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_INFLIGHT_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_INFLIGHT_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_INFLIGHT_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_INFLIGHT_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_EXPORTED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_EXPORTED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_LOG_EXPORTED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_LOG_EXPORTED_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_DESCRIPTION);

    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_NAME)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_NAME);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_UNIT)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_UNIT);
    assertThat(SemConvConstants.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_DESCRIPTION)
        .isEqualTo(OtelIncubatingMetrics.OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_DESCRIPTION);
  }
}
