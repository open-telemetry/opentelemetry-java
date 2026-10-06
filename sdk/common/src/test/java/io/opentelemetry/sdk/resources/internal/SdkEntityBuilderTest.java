/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SdkEntityBuilderTest {

  @ParameterizedTest
  @MethodSource("invalidTypes")
  void invalidType(String type) {
    assertThatThrownBy(() -> Entity.builder(type, Attributes.of(stringKey("id"), "1")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity type must be a valid non-empty printable ASCII string.");
  }

  static Stream<Arguments> invalidTypes() {
    return Stream.of(
        Arguments.argumentSet("empty", ""),
        Arguments.argumentSet("non-ASCII", "höst"),
        Arguments.argumentSet("control character", "host\n"),
        Arguments.argumentSet("too long", String.join("", Collections.nCopies(256, "a"))));
  }

  @Test
  void emptyIdentity() {
    assertThatThrownBy(() -> Entity.builder("host", Attributes.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entity must have at least one identifying attribute.");
  }

  @ParameterizedTest
  @MethodSource("overlappingDescriptions")
  void rejectsOverlappingDescription(Attributes description) {
    EntityBuilder builder = Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"));
    assertThatThrownBy(() -> builder.setDescription(description))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Identifying and descriptive attribute keys must be disjoint.");
    assertThat(builder.build().getDescription()).isEqualTo(Attributes.empty());
  }

  static Stream<Arguments> overlappingDescriptions() {
    return Stream.of(
        Arguments.argumentSet("same value", Attributes.of(stringKey("host.id"), "H1")),
        Arguments.argumentSet("changed value", Attributes.of(stringKey("host.id"), "H2")),
        Arguments.argumentSet("changed type", Attributes.of(AttributeKey.longKey("host.id"), 1L)));
  }

  @Test
  void emptySchemaIsAbsent() {
    Entity entity =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1")).setSchemaUrl("").build();
    assertThat(entity.getSchemaUrl()).isNull();
    assertThat(entity.toBuilder().build()).isEqualTo(entity);
  }

  @Test
  void freezingArraysPreservesValueAttributeKeyTypes() {
    List<String> values = new ArrayList<>(Arrays.asList("one", "two"));
    Attributes id =
        Attributes.of(
            AttributeKey.valueKey("id"),
            Value.of("H1"),
            AttributeKey.stringArrayKey("array"),
            values);
    Entity entity = Entity.builder("host", id).build();
    values.set(0, "changed");
    assertThat(entity.getId().asMap()).containsKey(AttributeKey.valueKey("id"));
    assertThat(entity.getId().get(AttributeKey.stringArrayKey("array")))
        .containsExactly("one", "two");
  }

  @Test
  void nullInputs() {
    assertThatThrownBy(() -> Entity.builder(null, Attributes.of(stringKey("id"), "1")))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("entityType");
    assertThatThrownBy(() -> Entity.builder("host", null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("id");
    assertThatThrownBy(
            () -> Entity.builder("host", Attributes.of(stringKey("id"), "1")).setDescription(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void validConstructionAndCopy() {
    Entity entity =
        Entity.builder("host", Attributes.of(stringKey("host.id"), "H1"))
            .setDescription(Attributes.of(stringKey("host.name"), "machine"))
            .setSchemaUrl("https://opentelemetry.io/schemas/1.40.0")
            .build();
    assertThat(entity.toBuilder().build()).isEqualTo(entity);
  }
}
