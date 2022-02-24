/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class NameSanitizerTest {

  @Test
  void testSanitizerCaching() {
    AtomicInteger count = new AtomicInteger();
    Function<String, String> delegate = labelName -> labelName + count.incrementAndGet();
    NameSanitizer sanitizer = new NameSanitizer(delegate);
    String labelName = "http.name";

    assertThat(sanitizer.apply(labelName)).isEqualTo("http.name1");
    assertThat(sanitizer.apply(labelName)).isEqualTo("http.name1");
    assertThat(sanitizer.apply(labelName)).isEqualTo("http.name1");
    assertThat(sanitizer.apply(labelName)).isEqualTo("http.name1");
    assertThat(sanitizer.apply(labelName)).isEqualTo("http.name1");
    assertThat(count).hasValue(1);
  }
}
