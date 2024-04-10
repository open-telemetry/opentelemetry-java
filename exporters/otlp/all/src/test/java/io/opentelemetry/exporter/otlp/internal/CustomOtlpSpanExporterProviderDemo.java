/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import java.util.Collections;
import java.util.Map;

/** This is just a demo - not a test (yet). */
public class CustomOtlpSpanExporterProviderDemo {

  public static void main(String[] args) {
    CustomOtlpSpanExporterProviderDemo demo = new CustomOtlpSpanExporterProviderDemo();

    OtlpSpanExporterProvider.createHttpProtobufExporter(c -> c.setHeaders(demo::headers));
  }

  private Map<String, String> headers() {
    return Collections.singletonMap("Authorization", "Bearer " + refreshToken());
  }

  private String refreshToken() {
    // e.g. read the token from a kubernetes secret
    return "token";
  }
}
