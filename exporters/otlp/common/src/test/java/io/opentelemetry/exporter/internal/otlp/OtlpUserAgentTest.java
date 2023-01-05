/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OtlpUserAgentTest {

  @Test
  void getUserAgent() {
    assertThat(OtlpUserAgent.getUserAgent()).matches("OTel-OTLP-Exporter-Java/1\\..*");
  }

  @Test
  void addUserAgentHeader() {
    AtomicReference<String> keyRef = new AtomicReference<>();
    AtomicReference<String> valueRef = new AtomicReference<>();
    OtlpUserAgent.addUserAgentHeader(
        (key, value) -> {
          keyRef.set(key);
          valueRef.set(value);
        });
    assertThat(keyRef.get()).isEqualTo("User-Agent");
    assertThat(valueRef.get()).matches("OTel-OTLP-Exporter-Java/1\\..*");
  }
}
