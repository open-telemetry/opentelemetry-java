/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class LabelNameSanitizerTest {

  @Test
  void testSanitizerCaching() {
    AtomicInteger count = new AtomicInteger();
    Function<String, String> delegate = labelName -> labelName + count.incrementAndGet();
    LabelNameSanitizer sanitizer = new LabelNameSanitizer(delegate);
    String labelName = "http.name";

    assertEquals("http.name1", sanitizer.apply(labelName));
    assertEquals("http.name1", sanitizer.apply(labelName));
    assertEquals("http.name1", sanitizer.apply(labelName));
    assertEquals("http.name1", sanitizer.apply(labelName));
    assertEquals("http.name1", sanitizer.apply(labelName));
    assertEquals(1, count.get());
  }
}
