/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.OkHttp;
import org.junit.jupiter.api.Test;

class OkHttpVersionTest {
  @Test
  void expectedOkHttpVersion() {
    String expectedMajorVersion = System.getProperty("expectedOkHttpMajorVersion");
    assertThat(OkHttp.VERSION)
        .withFailMessage(
            "Expected OkHttp major version %s but got %s", expectedMajorVersion, OkHttp.VERSION)
        .startsWith(expectedMajorVersion);
  }
}
