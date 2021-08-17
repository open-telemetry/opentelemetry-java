/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class MeterSelectorTest {

  @Test
  void nameSelection_works() {
    MeterSelector exactName = MeterSelector.builder().setName("example").build();
    assertThat(exactName.getNameFilter().test("example")).isTrue();
    assertThat(exactName.getNameFilter().test("example2")).isFalse();

    MeterSelector patternName =
        MeterSelector.builder().setNamePattern(Pattern.compile("ex.*")).build();
    assertThat(patternName.getNameFilter().test("example")).isTrue();
    assertThat(patternName.getNameFilter().test("example2")).isTrue();
    assertThat(patternName.getNameFilter().test("axample")).isFalse();

    MeterSelector filterName =
        MeterSelector.builder().setNameFilter(name -> name.startsWith("ex")).build();
    assertThat(filterName.getNameFilter().test("example")).isTrue();
    assertThat(filterName.getNameFilter().test("example2")).isTrue();
    assertThat(filterName.getNameFilter().test("axample")).isFalse();
  }

  @Test
  void nameSelection_lastFilterWins() {
    MeterSelector filterName =
        MeterSelector.builder()
            .setName("example")
            .setNamePattern(Pattern.compile("ex.*"))
            .setNameFilter(name -> false)
            .build();

    assertThat(filterName.getNameFilter().test("example")).isFalse();
  }

  @Test
  void versionSelection_works() {
    MeterSelector exactVersion = MeterSelector.builder().setVersion("1.2.3").build();
    assertThat(exactVersion.getVersionFilter().test("1.2.3")).isTrue();
    assertThat(exactVersion.getVersionFilter().test("1.2.4")).isFalse();

    MeterSelector patternVersion =
        MeterSelector.builder().setVersionPattern(Pattern.compile("1\\.2\\..*")).build();
    assertThat(patternVersion.getVersionFilter().test("1.2.3")).isTrue();
    assertThat(patternVersion.getVersionFilter().test("1.2.4")).isTrue();
    assertThat(patternVersion.getVersionFilter().test("2.0.0")).isFalse();

    MeterSelector filterVersion =
        MeterSelector.builder().setVersionFilter(v -> v.startsWith("1")).build();
    assertThat(filterVersion.getVersionFilter().test("1.2.3")).isTrue();
    assertThat(filterVersion.getVersionFilter().test("1.1.1")).isTrue();
    assertThat(filterVersion.getVersionFilter().test("2.0.0")).isFalse();
  }

  @Test
  void versionSelection_lastFilterWins() {
    MeterSelector filterVersion =
        MeterSelector.builder()
            .setVersion("1.0")
            .setVersionPattern(Pattern.compile("1.*"))
            .setVersionFilter(name -> false)
            .build();

    assertThat(filterVersion.getVersionFilter().test("1.0")).isFalse();
    assertThat(filterVersion.getVersionFilter().test("1.2")).isFalse();
  }

  @Test
  void schemaUrlSelection_works() {
    MeterSelector exact = MeterSelector.builder().setSchemaUrl("1.2.3").build();
    assertThat(exact.getSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(exact.getSchemaUrlFilter().test("1.2.4")).isFalse();

    MeterSelector pattern =
        MeterSelector.builder().setSchemaUrlPattern(Pattern.compile("1\\.2\\..*")).build();
    assertThat(pattern.getSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(pattern.getSchemaUrlFilter().test("1.2.4")).isTrue();
    assertThat(pattern.getSchemaUrlFilter().test("2.0.0")).isFalse();

    MeterSelector filter =
        MeterSelector.builder().setSchemaUrlFilter(s -> s.startsWith("1")).build();
    assertThat(filter.getSchemaUrlFilter().test("1.2.3")).isTrue();
    assertThat(filter.getSchemaUrlFilter().test("1.1.1")).isTrue();
    assertThat(filter.getSchemaUrlFilter().test("2.0.0")).isFalse();
  }

  @Test
  void schemaUrlSelection_lastFilterWins() {
    MeterSelector schemaUrl =
        MeterSelector.builder()
            .setSchemaUrl("1.0")
            .setSchemaUrlPattern(Pattern.compile("1.*"))
            .setSchemaUrlFilter(s -> false)
            .build();

    assertThat(schemaUrl.getSchemaUrlFilter().test("1.0")).isFalse();
    assertThat(schemaUrl.getSchemaUrlFilter().test("1.2")).isFalse();
  }
}
