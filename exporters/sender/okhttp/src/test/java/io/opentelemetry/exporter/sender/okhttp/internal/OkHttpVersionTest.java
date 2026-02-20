/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;

import okhttp3.OkHttp;
import org.junit.jupiter.api.Test;

class OkHttpVersionTest {
  @Test
  void expectedOkHttpVersion() {
    String expectedMajorVersion = System.getProperty("expectedOkHttpMajorVersion");
    assertTrue(
        OkHttp.VERSION.startsWith(expectedMajorVersion),
        "Expected OkHttp major version "
            + expectedMajorVersion
            + " but got "
            + OkHttp.VERSION);
  }
}
