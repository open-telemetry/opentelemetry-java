/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class LegacyYamlDeclarativeConfigPropertiesTest {

  @Test
  void delegation() {
    DeclarativeConfigProperties delegate = mock(DeclarativeConfigProperties.class);
    DeclarativeConfigProperties child = mock(DeclarativeConfigProperties.class);
    ComponentLoader componentLoader = mock(ComponentLoader.class);

    when(delegate.getString("str")).thenReturn("hello");
    when(delegate.getBoolean("bool")).thenReturn(true);
    when(delegate.getInt("int")).thenReturn(42);
    when(delegate.getLong("long")).thenReturn(100L);
    when(delegate.getDouble("double")).thenReturn(3.14);

    when(delegate.isString("str")).thenReturn(true);
    when(delegate.isBoolean("bool")).thenReturn(true);
    when(delegate.isInt("int")).thenReturn(true);
    when(delegate.isLong("long")).thenReturn(true);
    when(delegate.isDouble("double")).thenReturn(true);

    when(delegate.getScalarList("list", String.class))
        .thenReturn(Collections.singletonList("item"));
    when(delegate.getStructured("child")).thenReturn(child);
    when(delegate.getStructured("missing")).thenReturn(null);

    when(delegate.getStructuredList("children")).thenReturn(Collections.singletonList(child));
    when(delegate.getStructuredList("missing_list")).thenReturn(null);

    when(delegate.getPropertyKeys()).thenReturn(Collections.singleton("str"));
    when(delegate.getComponentLoader()).thenReturn(componentLoader);
    when(delegate.toString()).thenReturn("mockDelegate");

    LegacyYamlDeclarativeConfigProperties properties =
        new LegacyYamlDeclarativeConfigProperties(delegate);

    assertThat(properties.getString("str")).isEqualTo("hello");
    assertThat(properties.getBoolean("bool")).isTrue();
    assertThat(properties.getInt("int")).isEqualTo(42);
    assertThat(properties.getLong("long")).isEqualTo(100L);
    assertThat(properties.getDouble("double")).isEqualTo(3.14);

    assertThat(properties.isString("str")).isTrue();
    assertThat(properties.isBoolean("bool")).isTrue();
    assertThat(properties.isInt("int")).isTrue();
    assertThat(properties.isLong("long")).isTrue();
    assertThat(properties.isDouble("double")).isTrue();

    assertThat(properties.getScalarList("list", String.class)).containsExactly("item");

    io.opentelemetry.api.incubator.config.DeclarativeConfigProperties structured =
        properties.getStructured("child");
    assertThat(structured).isInstanceOf(LegacyYamlDeclarativeConfigProperties.class);
    assertThat(properties.getStructured("missing")).isNull();

    List<io.opentelemetry.api.incubator.config.DeclarativeConfigProperties> structuredList =
        properties.getStructuredList("children");
    assertThat(structuredList).hasSize(1);
    assertThat(structuredList.get(0)).isInstanceOf(LegacyYamlDeclarativeConfigProperties.class);
    assertThat(properties.getStructuredList("missing_list")).isNull();

    assertThat(properties.getPropertyKeys()).containsExactly("str");
    assertThat(properties.getComponentLoader()).isSameAs(componentLoader);
    assertThat(properties.toString())
        .isEqualTo("LegacyYamlDeclarativeConfigProperties{mockDelegate}");
  }
}
