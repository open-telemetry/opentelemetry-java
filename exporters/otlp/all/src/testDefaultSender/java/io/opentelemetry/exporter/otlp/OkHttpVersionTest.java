/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttp;
import org.junit.jupiter.api.Test;

class OkHttpVersionTest {

  private static final Logger LOGGER = Logger.getLogger(OkHttpVersionTest.class.getName());

  @Test
  void expectedOkHttpVersion() {
    String expectedVersion = System.getProperty("expected.okhttp.version");
    LOGGER.log(Level.WARNING, "Testing okhttp version " + expectedVersion);
    assumeThat(expectedVersion.equals("LATEST")).isFalse();
    assertThat(OkHttp.VERSION).isEqualTo(expectedVersion);
  }
}
