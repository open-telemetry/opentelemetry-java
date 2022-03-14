/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

// Testing deprecated class.
@SuppressWarnings("deprecation")
public class MeterSelectorTest {

  @Test
  void nameSelection_works() {
    MeterSelector exactName = MeterSelector.builder().setName("example").build();
    assertThat(exactName.getNameFilter().test("example")).isTrue();
    assertThat(exactName.getNameFilter().test("example2")).isFalse();

    MeterSelector filterName =
        MeterSelector.builder().setName(name -> name.startsWith("ex")).build();
    assertThat(filterName.getNameFilter().test("example")).isTrue();
    assertThat(filterName.getNameFilter().test("example2")).isTrue();
    assertThat(filterName.getNameFilter().test("axample")).isFalse();
  }

  @Test
  void nameSelection_lastFilterWins() {
    MeterSelector filterName =
        MeterSelector.builder()
            .setName("example")
            .setName(name -> name.startsWith("ex"))
            .setName(name -> false)
            .build();

    assertThat(filterName.getNameFilter().test("example")).isFalse();
  }

  @Test
  void versionSelection_works() {
    MeterSelector exactVersion = MeterSelector.builder().setVersion("1.2.3").build();
    assertThat(exactVersion.getVersionFilter().test("1.2.3")).isTrue();
    assertThat(exactVersion.getVersionFilter().test("1.2.4")).isFalse();

    MeterSelector filterVersion =
        MeterSelector.builder().setVersion(v -> v.startsWith("1")).build();
    assertThat(filterVersion.getVersionFilter().test("1.2.3")).isTrue();
    assertThat(filterVersion.getVersionFilter().test("1.1.1")).isTrue();
    assertThat(filterVersion.getVersionFilter().test("2.0.0")).isFalse();
  }

  @Test
  void versionSelection_lastFilterWins() {
    MeterSelector filterVersion =
        MeterSelector.builder()
            .setVersion("1.0")
            .setVersion(name -> name.startsWith("1"))
            .setVersion(name -> false)
            .build();

    assertThat(filterVersion.getVersionFilter().test("1.0")).isFalse();
    assertThat(filterVersion.getVersionFilter().test("1.2")).isFalse();
  }

  @Test
  void schemaUrlSelection_works() {
    MeterSelector exact = MeterSelector.builder().setSchemaUrl("1.2.3").build();
    assertThat(exact.getSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(exact.getSchemaUrlFilter().test("1.2.4")).isFalse();

    MeterSelector filter = MeterSelector.builder().setSchemaUrl(s -> s.startsWith("1")).build();
    assertThat(filter.getSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(filter.getSchemaUrlFilter().test("1.1.1")).isTrue();
    assertThat(filter.getSchemaUrlFilter().test("2.0.0")).isFalse();
  }

  @Test
  void schemaUrlSelection_lastFilterWins() {
    MeterSelector schemaUrl =
        MeterSelector.builder()
            .setSchemaUrl("1.0")
            .setSchemaUrl(name -> name.startsWith("1"))
            .setSchemaUrl(s -> false)
            .build();

    assertThat(schemaUrl.getSchemaUrlFilter().test("1.0")).isFalse();
    assertThat(schemaUrl.getSchemaUrlFilter().test("1.2")).isFalse();
  }

  @Test
  void invalidArgs() {
    assertThatThrownBy(() -> MeterSelector.builder().setName((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("nameFilter");
    assertThatThrownBy(() -> MeterSelector.builder().setName((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
    assertThatThrownBy(() -> MeterSelector.builder().setVersion((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("versionFilter");
    assertThatThrownBy(() -> MeterSelector.builder().setVersion((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("version");
    assertThatThrownBy(() -> MeterSelector.builder().setSchemaUrl((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("schemaUrlFilter");
    assertThatThrownBy(() -> MeterSelector.builder().setSchemaUrl((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("schemaUrl");
  }
}
