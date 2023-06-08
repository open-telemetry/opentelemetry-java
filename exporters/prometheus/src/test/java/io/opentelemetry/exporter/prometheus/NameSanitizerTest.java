/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest
  @MethodSource("provideMetricNamesForTest")
  void testSanitizerCleansing(String unsanitizedName, String sanitizedName) {
    Assertions.assertEquals(sanitizedName, NameSanitizer.INSTANCE.apply(unsanitizedName));
  }

  private static Stream<Arguments> provideMetricNamesForTest() {
    return Stream.of(
        // valid name - already sanitized
        Arguments.of(
            "active_directory_ds_replication_network_io",
            "active_directory_ds_replication_network_io"),
        // consecutive underscores
        Arguments.of("cpu_sp__d_hertz", "cpu_sp_d_hertz"),
        // leading and trailing underscores - should be fine
        Arguments.of("_cpu_speed_hertz_", "_cpu_speed_hertz_"),
        // unsupported characters replaced
        Arguments.of("metric_unit_$1000", "metric_unit_1000"),
        // multiple unsupported characters - whitespace
        Arguments.of("sample_me%%$$$_count_ !!@unit include", "sample_me_count_unit_include"),
        // metric names cannot start with a number
        Arguments.of("1_some_metric_name", "_some_metric_name"),
        // metric names can have :
        Arguments.of("sample_metric_name__:_per_meter", "sample_metric_name_:_per_meter"),
        // Illegal characters
        Arguments.of("cpu_sp$$d_hertz", "cpu_sp_d_hertz"));
  }
}
