/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

/**
 * Helper for recording metrics from OTLP exporters.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ExporterMetrics {

  private static final AttributeKey<String> ATTRIBUTE_KEY_TYPE = stringKey("type");
  private static final AttributeKey<Boolean> ATTRIBUTE_KEY_SUCCESS = booleanKey("success");

  private final BoundLongCounter seen;
  private final BoundLongCounter success;
  private final BoundLongCounter failed;

  private ExporterMetrics(Meter meter, String type) {
    Attributes attributes = Attributes.builder().put(ATTRIBUTE_KEY_TYPE, type).build();
    seen = meter.counterBuilder("otlp.exporter.seen").build().bind(attributes);
    LongCounter exported = meter.counterBuilder("otlp.exporter.exported").build();
    success = exported.bind(attributes.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, true).build());
    failed = exported.bind(attributes.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, false).build());
  }

  /** Record number of records seen. */
  public void addSeen(long value) {
    seen.add(value);
  }

  /** Record number of records which successfully exported. */
  public void addSuccess(long value) {
    success.add(value);
  }

  /** Record number of records which failed to export. */
  public void addFailed(long value) {
    failed.add(value);
  }

  /** Unbind the instruments. */
  public void unbind() {
    seen.unbind();
    success.unbind();
    failed.unbind();
  }

  /** Create an instance for recording OTLP gRPC exporter metrics. */
  public static ExporterMetrics createGrpc(String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider.get("io.opentelemetry.exporters.otlp-grpc"), type);
  }

  /** Create an instance for recording OTLP http/protobuf exporter metrics. */
  public static ExporterMetrics createHttpProtobuf(String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider.get("io.opentelemetry.exporters.otlp-http"), type);
  }
}
