/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;

/**
 * Helper for recording metrics from exporters.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ExporterMetrics {

  private static final AttributeKey<String> ATTRIBUTE_KEY_TYPE = stringKey("type");
  private static final AttributeKey<Boolean> ATTRIBUTE_KEY_SUCCESS = booleanKey("success");

  private final LongCounter seen;
  private final LongCounter exported;

  private final Attributes seenAttrs;
  private final Attributes successAttrs;
  private final Attributes failedAttrs;

  private ExporterMetrics(
      MeterProvider meterProvider, String exporterName, String type, String transportName) {
    Meter meter =
        meterProvider.get("io.opentelemetry.exporters." + exporterName + "-" + transportName);
    seenAttrs = Attributes.builder().put(ATTRIBUTE_KEY_TYPE, type).build();
    seen = meter.counterBuilder(exporterName + ".exporter.seen").build();
    exported = meter.counterBuilder(exporterName + ".exporter.exported").build();
    successAttrs = seenAttrs.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, true).build();
    failedAttrs = seenAttrs.toBuilder().put(ATTRIBUTE_KEY_SUCCESS, false).build();
  }

  /** Record number of records seen. */
  public void addSeen(long value) {
    seen.add(value, seenAttrs);
  }

  /** Record number of records which successfully exported. */
  public void addSuccess(long value) {
    exported.add(value, successAttrs);
  }

  /** Record number of records which failed to export. */
  public void addFailed(long value) {
    exported.add(value, failedAttrs);
  }

  /**
   * Create an instance for recording exporter metrics under the meter {@code
   * "io.opentelemetry.exporters." + exporterName + "-grpc}".
   */
  public static ExporterMetrics createGrpc(
      String exporterName, String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider, exporterName, type, "grpc");
  }

  /**
   * Create an instance for recording exporter metrics under the meter {@code
   * "io.opentelemetry.exporters." + exporterName + "-grpc-okhttp}".
   */
  public static ExporterMetrics createGrpcOkHttp(
      String exporterName, String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider, exporterName, type, "grpc-okhttp");
  }

  /**
   * Create an instance for recording exporter metrics under the meter {@code
   * "io.opentelemetry.exporters." + exporterName + "-http}".
   */
  public static ExporterMetrics createHttpProtobuf(
      String exporterName, String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider, exporterName, type, "http");
  }

  /**
   * Create an instance for recording exporter metrics under the meter {@code
   * "io.opentelemetry.exporters." + exporterName + "-http-json}".
   */
  public static ExporterMetrics createHttpJson(
      String exporterName, String type, MeterProvider meterProvider) {
    return new ExporterMetrics(meterProvider, exporterName, type, "http-json");
  }
}
