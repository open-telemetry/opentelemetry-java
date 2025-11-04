/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableFunctionData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableKeyValueAndUnitData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLinkData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableLocationData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableMappingData;
import io.opentelemetry.exporter.otlp.internal.data.ImmutableStackData;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileDictionaryCompositorTest {

  ProfileDictionaryCompositor compositor;

  @BeforeEach
  void setUp() {
    compositor = new ProfileDictionaryCompositor();
  }

  @Test
  void hasInitialZeroElements() {
    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getMappingTable()).size().isEqualTo(1);
    assertThat(data.getLocationTable()).size().isEqualTo(1);
    assertThat(data.getFunctionTable()).size().isEqualTo(1);
    assertThat(data.getLinkTable()).size().isEqualTo(1);
    assertThat(data.getStringTable()).size().isEqualTo(1);
    assertThat(data.getAttributeTable()).size().isEqualTo(1);
    assertThat(data.getStackTable()).size().isEqualTo(1);
  }

  @Test
  void handlesMappings() {
    MappingData a = ImmutableMappingData.create(1, 2, 3, 4, Collections.emptyList());
    MappingData b = ImmutableMappingData.create(2, 3, 4, 5, Collections.emptyList());

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getMappingTable()).size().isEqualTo(3);
    assertThat(data.getMappingTable().get(1)).isEqualTo(a);
    assertThat(data.getMappingTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesLocations() {
    LocationData a =
        ImmutableLocationData.create(1, 2, Collections.emptyList(), Collections.emptyList());
    LocationData b =
        ImmutableLocationData.create(3, 4, Collections.emptyList(), Collections.emptyList());

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getLocationTable()).size().isEqualTo(3);
    assertThat(data.getLocationTable().get(1)).isEqualTo(a);
    assertThat(data.getLocationTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesFunctions() {
    FunctionData a = ImmutableFunctionData.create(1, 2, 3, 4);
    FunctionData b = ImmutableFunctionData.create(5, 6, 7, 8);

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getFunctionTable()).size().isEqualTo(3);
    assertThat(data.getFunctionTable().get(1)).isEqualTo(a);
    assertThat(data.getFunctionTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesLinks() {
    LinkData a = ImmutableLinkData.create("a1", "a2");
    LinkData b = ImmutableLinkData.create("b1", "b2");

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getLinkTable()).size().isEqualTo(3);
    assertThat(data.getLinkTable().get(1)).isEqualTo(a);
    assertThat(data.getLinkTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesStrings() {
    String a = "foo";
    String b = "bar";

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getStringTable()).size().isEqualTo(3);
    assertThat(data.getStringTable().get(1)).isEqualTo(a);
    assertThat(data.getStringTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesAttributes() {
    KeyValueAndUnitData a = ImmutableKeyValueAndUnitData.create(1, Value.of("a"), 2);
    KeyValueAndUnitData b = ImmutableKeyValueAndUnitData.create(3, Value.of("b"), 4);

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getAttributeTable()).size().isEqualTo(3);
    assertThat(data.getAttributeTable().get(1)).isEqualTo(a);
    assertThat(data.getAttributeTable().get(2)).isEqualTo(b);
  }

  @Test
  void handlesStacks() {
    StackData a = ImmutableStackData.create(Arrays.asList(1, 2));
    StackData b = ImmutableStackData.create(Arrays.asList(3, 4));

    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(a)).isEqualTo(1);
    assertThat(compositor.putIfAbsent(b)).isEqualTo(2);

    ProfileDictionaryData data = compositor.getProfileDictionaryData();
    assertThat(data.getStackTable()).size().isEqualTo(3);
    assertThat(data.getStackTable().get(1)).isEqualTo(a);
    assertThat(data.getStackTable().get(2)).isEqualTo(b);
  }
}
