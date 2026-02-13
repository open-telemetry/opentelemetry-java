/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.OkHttp;
import org.junit.jupiter.api.Test;

class OkHttp4VersionTest {
  @Test
  void expectedOkHttpVersion() {
    assertThat(OkHttp.VERSION).startsWith("4");
  }
}
