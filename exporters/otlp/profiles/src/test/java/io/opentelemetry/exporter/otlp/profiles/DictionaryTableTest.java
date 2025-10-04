/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DictionaryTableTest {

  DictionaryTable<String> table;

  @BeforeEach
  void setUp() {
    table = new DictionaryTable<>();
  }

  @Test
  void isInitiallyEmpty() {
    assertThat(table.getTable()).isEmpty();
  }

  @Test
  void isDeduplicating() {
    assertThat(table.putIfAbsent("foo")).isEqualTo(0);
    assertThat(table.putIfAbsent("foo")).isEqualTo(0);
    assertThat(table.getTable()).size().isEqualTo(1);
  }

  @Test
  void isSequencing() {
    assertThat(table.putIfAbsent("foo")).isEqualTo(0);
    assertThat(table.putIfAbsent("bar")).isEqualTo(1);
    assertThat(table.getTable()).size().isEqualTo(2);
    assertThat(table.getTable().get(0)).isEqualTo("foo");
    assertThat(table.getTable().get(1)).isEqualTo("bar");
  }
}
