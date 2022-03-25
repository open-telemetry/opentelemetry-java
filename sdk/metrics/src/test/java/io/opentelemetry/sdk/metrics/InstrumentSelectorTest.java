/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class InstrumentSelectorTest {

  @Test
  void invalidArgs() {
    assertThatThrownBy(() -> InstrumentSelector.builder().setType(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("instrumentType");
    assertThatThrownBy(() -> InstrumentSelector.builder().setName((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("nameFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().setName((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterName((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterName");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterName((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterNameFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterVersion((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterVersion");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterVersion((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterVersionFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().setMeterSchemaUrl((String) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterSchemaUrl");
    assertThatThrownBy(
            () -> InstrumentSelector.builder().setMeterSchemaUrl((Predicate<String>) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("meterSchemaUrlFilter");
    assertThatThrownBy(() -> InstrumentSelector.builder().build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Instrument selector must contain selection criteria");
  }

  @Test
  void meterNameSelection_works() {
    InstrumentSelector exactName = InstrumentSelector.builder().setMeterName("example").build();
    assertThat(exactName.getMeterNameFilter().test("example")).isTrue();
    assertThat(exactName.getMeterNameFilter().test("example2")).isFalse();

    InstrumentSelector filterName =
        InstrumentSelector.builder().setMeterName(name -> name.startsWith("ex")).build();
    assertThat(filterName.getMeterNameFilter().test("example")).isTrue();
    assertThat(filterName.getMeterNameFilter().test("example2")).isTrue();
    assertThat(filterName.getMeterNameFilter().test("axample")).isFalse();
  }

  @Test
  void meterNameSelection_lastFilterWins() {
    InstrumentSelector filterName =
        InstrumentSelector.builder()
            .setMeterName("example")
            .setMeterName(name -> name.startsWith("ex"))
            .setMeterName(name -> false)
            .build();

    assertThat(filterName.getMeterNameFilter().test("example")).isFalse();
  }

  @Test
  void versionSelection_works() {
    InstrumentSelector exactVersion = InstrumentSelector.builder().setMeterVersion("1.2.3").build();
    assertThat(exactVersion.getMeterVersionFilter().test("1.2.3")).isTrue();
    assertThat(exactVersion.getMeterVersionFilter().test("1.2.4")).isFalse();

    InstrumentSelector filterVersion =
        InstrumentSelector.builder().setMeterVersion(v -> v.startsWith("1")).build();
    assertThat(filterVersion.getMeterVersionFilter().test("1.2.3")).isTrue();
    assertThat(filterVersion.getMeterVersionFilter().test("1.1.1")).isTrue();
    assertThat(filterVersion.getMeterVersionFilter().test("2.0.0")).isFalse();
  }

  @Test
  void versionSelection_lastFilterWins() {
    InstrumentSelector filterVersion =
        InstrumentSelector.builder()
            .setMeterVersion("1.0")
            .setMeterVersion(name -> name.startsWith("1"))
            .setMeterVersion(name -> false)
            .build();

    assertThat(filterVersion.getMeterVersionFilter().test("1.0")).isFalse();
    assertThat(filterVersion.getMeterVersionFilter().test("1.2")).isFalse();
  }

  @Test
  void schemaUrlSelection_works() {
    InstrumentSelector exact = InstrumentSelector.builder().setMeterSchemaUrl("1.2.3").build();
    assertThat(exact.getMeterSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(exact.getMeterSchemaUrlFilter().test("1.2.4")).isFalse();

    InstrumentSelector filter =
        InstrumentSelector.builder().setMeterSchemaUrl(s -> s.startsWith("1")).build();
    assertThat(filter.getMeterSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(filter.getMeterSchemaUrlFilter().test("1.1.1")).isTrue();
    assertThat(filter.getMeterSchemaUrlFilter().test("2.0.0")).isFalse();
  }

  @Test
  void schemaUrlSelection_lastFilterWins() {
    InstrumentSelector schemaUrl =
        InstrumentSelector.builder()
            .setMeterSchemaUrl("1.0")
            .setMeterSchemaUrl(name -> name.startsWith("1"))
            .setMeterSchemaUrl(s -> false)
            .build();

    assertThat(schemaUrl.getMeterSchemaUrlFilter().test("1.0")).isFalse();
    assertThat(schemaUrl.getMeterSchemaUrlFilter().test("1.2")).isFalse();
  }
}
