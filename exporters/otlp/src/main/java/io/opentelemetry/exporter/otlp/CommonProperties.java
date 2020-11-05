/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

public class CommonProperties {
  public static final String KEY_TIMEOUT = "otel.exporter.otlp.timeout";
  public static final String KEY_ENDPOINT = "otel.exporter.otlp.endpoint";
  public static final String KEY_INSECURE = "otel.exporter.otlp.insecure";
  public static final String KEY_HEADERS = "otel.exporter.otlp.headers";

  private CommonProperties() {}
}
