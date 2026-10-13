/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.AttributeKey;

/**
 * Provides access to semantic convention attributes, metric names, units, and descriptions used
 * within the SDK implementation. This avoids having to pull in semantic conventions as a
 * dependency, which would easily collide and conflict with user-provided dependencies.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class SemConvConstants {

  private SemConvConstants() {}

  // Schema url

  // TODO(jack-berg): decide when and why we change schema url
  public static final String SCHEMA_URL_V1_40_0 = "https://opentelemetry.io/schemas/1.40.0";

  // Entity types

  public static final String SERVICE_TYPE = "service";
  public static final String SERVICE_INSTANCE_TYPE = "service.instance";

  // Attributes

  public static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  public static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  public static final AttributeKey<String> OTEL_COMPONENT_TYPE =
      AttributeKey.stringKey("otel.component.type");
  public static final AttributeKey<String> OTEL_COMPONENT_NAME =
      AttributeKey.stringKey("otel.component.name");
  public static final AttributeKey<String> ERROR_TYPE = AttributeKey.stringKey("error.type");

  public static final AttributeKey<String> SERVER_ADDRESS =
      AttributeKey.stringKey("server.address");
  public static final AttributeKey<Long> SERVER_PORT = AttributeKey.longKey("server.port");

  public static final AttributeKey<String> RPC_RESPONSE_STATUS_CODE =
      AttributeKey.stringKey("rpc.response.status_code");
  public static final AttributeKey<Long> HTTP_RESPONSE_STATUS_CODE =
      AttributeKey.longKey("http.response.status_code");

  public static final AttributeKey<String> OTEL_SPAN_PARENT_ORIGIN =
      AttributeKey.stringKey("otel.span.parent.origin");
  public static final AttributeKey<String> OTEL_SPAN_SAMPLING_RESULT =
      AttributeKey.stringKey("otel.span.sampling_result");

  public static final String OTEL_SDK_SPAN_STARTED_NAME = "otel.sdk.span.started";
  public static final String OTEL_SDK_SPAN_STARTED_UNIT = "{span}";
  public static final String OTEL_SDK_SPAN_STARTED_DESCRIPTION = "The number of created spans.";

  public static final String OTEL_SDK_SPAN_LIVE_NAME = "otel.sdk.span.live";
  public static final String OTEL_SDK_SPAN_LIVE_UNIT = "{span}";
  public static final String OTEL_SDK_SPAN_LIVE_DESCRIPTION =
      "The number of created spans with `recording=true` for which the end operation has not been called yet.";

  public static final String OTEL_SDK_PROCESSOR_SPAN_PROCESSED_NAME =
      "otel.sdk.processor.span.processed";
  public static final String OTEL_SDK_PROCESSOR_SPAN_PROCESSED_UNIT = "{span}";
  public static final String OTEL_SDK_PROCESSOR_SPAN_PROCESSED_DESCRIPTION =
      "The number of spans for which the processing has finished, either successful or failed.";

  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_NAME =
      "otel.sdk.processor.span.queue.capacity";
  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_UNIT = "{span}";
  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_CAPACITY_DESCRIPTION =
      "The maximum number of spans the queue of a given instance of an SDK span processor can hold.";

  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_NAME =
      "otel.sdk.processor.span.queue.size";
  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_UNIT = "{span}";
  public static final String OTEL_SDK_PROCESSOR_SPAN_QUEUE_SIZE_DESCRIPTION =
      "The number of spans in the queue of a given instance of an SDK span processor.";

  public static final String OTEL_SDK_LOG_CREATED_NAME = "otel.sdk.log.created";
  public static final String OTEL_SDK_LOG_CREATED_UNIT = "{log_record}";
  public static final String OTEL_SDK_LOG_CREATED_DESCRIPTION =
      "The number of logs submitted to enabled SDK Loggers.";

  public static final String OTEL_SDK_PROCESSOR_LOG_PROCESSED_NAME =
      "otel.sdk.processor.log.processed";
  public static final String OTEL_SDK_PROCESSOR_LOG_PROCESSED_UNIT = "{log_record}";
  public static final String OTEL_SDK_PROCESSOR_LOG_PROCESSED_DESCRIPTION =
      "The number of log records for which the processing has finished, either successful or failed.";

  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_NAME =
      "otel.sdk.processor.log.queue.capacity";
  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_UNIT = "{log_record}";
  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_CAPACITY_DESCRIPTION =
      "The maximum number of log records the queue of a given instance of an SDK Log Record processor can hold.";

  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_NAME =
      "otel.sdk.processor.log.queue.size";
  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_UNIT = "{log_record}";
  public static final String OTEL_SDK_PROCESSOR_LOG_QUEUE_SIZE_DESCRIPTION =
      "The number of log records in the queue of a given instance of an SDK log processor.";

  public static final String OTEL_SDK_METRIC_READER_COLLECTION_DURATION_NAME =
      "otel.sdk.metric_reader.collection.duration";
  public static final String OTEL_SDK_METRIC_READER_COLLECTION_DURATION_UNIT = "s";
  public static final String OTEL_SDK_METRIC_READER_COLLECTION_DURATION_DESCRIPTION =
      "The duration of the collect operation of the metric reader.";

  public static final String OTEL_SDK_EXPORTER_OPERATION_DURATION_NAME =
      "otel.sdk.exporter.operation.duration";
  public static final String OTEL_SDK_EXPORTER_OPERATION_DURATION_UNIT = "s";
  public static final String OTEL_SDK_EXPORTER_OPERATION_DURATION_DESCRIPTION =
      "The duration of exporting a batch of telemetry records.";

  public static final String OTEL_SDK_EXPORTER_SPAN_INFLIGHT_NAME =
      "otel.sdk.exporter.span.inflight";
  public static final String OTEL_SDK_EXPORTER_SPAN_INFLIGHT_UNIT = "{span}";
  public static final String OTEL_SDK_EXPORTER_SPAN_INFLIGHT_DESCRIPTION =
      "The number of spans which were passed to the exporter, but that have not been exported yet (neither successful, nor failed).";

  public static final String OTEL_SDK_EXPORTER_SPAN_EXPORTED_NAME =
      "otel.sdk.exporter.span.exported";
  public static final String OTEL_SDK_EXPORTER_SPAN_EXPORTED_UNIT = "{span}";
  public static final String OTEL_SDK_EXPORTER_SPAN_EXPORTED_DESCRIPTION =
      "The number of spans for which the export has finished, either successful or failed.";

  public static final String OTEL_SDK_EXPORTER_LOG_INFLIGHT_NAME = "otel.sdk.exporter.log.inflight";
  public static final String OTEL_SDK_EXPORTER_LOG_INFLIGHT_UNIT = "{log_record}";
  public static final String OTEL_SDK_EXPORTER_LOG_INFLIGHT_DESCRIPTION =
      "The number of log records which were passed to the exporter, but that have not been exported yet (neither successful, nor failed).";

  public static final String OTEL_SDK_EXPORTER_LOG_EXPORTED_NAME = "otel.sdk.exporter.log.exported";
  public static final String OTEL_SDK_EXPORTER_LOG_EXPORTED_UNIT = "{log_record}";
  public static final String OTEL_SDK_EXPORTER_LOG_EXPORTED_DESCRIPTION =
      "The number of log records for which the export has finished, either successful or failed.";

  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_NAME =
      "otel.sdk.exporter.metric_data_point.inflight";
  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_UNIT = "{data_point}";
  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_INFLIGHT_DESCRIPTION =
      "The number of metric data points which were passed to the exporter, but that have not been exported yet (neither successful, nor failed).";

  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_NAME =
      "otel.sdk.exporter.metric_data_point.exported";
  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_UNIT = "{data_point}";
  public static final String OTEL_SDK_EXPORTER_METRIC_DATA_POINT_EXPORTED_DESCRIPTION =
      "The number of metric data points for which the export has finished, either successful or failed.";
}
