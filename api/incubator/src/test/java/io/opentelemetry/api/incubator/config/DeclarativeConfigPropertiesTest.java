/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.ComponentLoader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DeclarativeConfigPropertiesTest {

  @Test
  void defaultMethodsFallBackWhenNull() {
    DeclarativeConfigProperties properties = DeclarativeConfigProperties.empty();

    assertThat(properties.getString("key", "defaultStr")).isEqualTo("defaultStr");
    assertThat(properties.getBoolean("key", true)).isTrue();
    assertThat(properties.getInt("key", 42)).isEqualTo(42);
    assertThat(properties.getLong("key", 100L)).isEqualTo(100L);
    assertThat(properties.getDouble("key", 3.14)).isEqualTo(3.14);
    assertThat(properties.getScalarList("key", String.class, Collections.singletonList("default")))
        .containsExactly("default");

    DeclarativeConfigProperties fallbackStructured = DeclarativeConfigProperties.empty();
    assertThat(properties.getStructured("key", fallbackStructured)).isSameAs(fallbackStructured);
    assertThat(properties.get("key")).isSameAs(DeclarativeConfigProperties.empty());

    List<DeclarativeConfigProperties> fallbackList = Collections.singletonList(fallbackStructured);
    assertThat(properties.getStructuredList("key", fallbackList)).isSameAs(fallbackList);
  }

  @Test
  void defaultMethodsDelegateWhenPresent() {
    DeclarativeConfigProperties child = DeclarativeConfigProperties.empty();
    DeclarativeConfigProperties properties =
        new DeclarativeConfigProperties() {
          @Override
          public String getString(String name) {
            return "actualStr";
          }

          @Override
          public Boolean getBoolean(String name) {
            return true;
          }

          @Override
          public Integer getInt(String name) {
            return 99;
          }

          @Override
          public Long getLong(String name) {
            return 999L;
          }

          @Override
          public Double getDouble(String name) {
            return 9.99;
          }

          @SuppressWarnings("unchecked")
          @Override
          public <T> List<T> getScalarList(String name, Class<T> scalarType) {
            return (List<T>) Collections.singletonList("item");
          }

          @Override
          public DeclarativeConfigProperties getStructured(String name) {
            return child;
          }

          @Override
          public List<DeclarativeConfigProperties> getStructuredList(String name) {
            return Collections.singletonList(child);
          }

          @Override
          public Set<String> getPropertyKeys() {
            return Collections.emptySet();
          }

          @Override
          public ComponentLoader getComponentLoader() {
            return ComponentLoader.forClassLoader(getClass().getClassLoader());
          }
        };

    assertThat(properties.getString("key", "defaultStr")).isEqualTo("actualStr");
    assertThat(properties.getBoolean("key", false)).isTrue();
    assertThat(properties.getInt("key", 42)).isEqualTo(99);
    assertThat(properties.getLong("key", 100L)).isEqualTo(999L);
    assertThat(properties.getDouble("key", 3.14)).isEqualTo(9.99);
    assertThat(properties.getScalarList("key", String.class, Collections.emptyList()))
        .containsExactly("item");

    DeclarativeConfigProperties fallbackStructured = DeclarativeConfigProperties.empty();
    assertThat(properties.getStructured("key", fallbackStructured)).isSameAs(child);
    assertThat(properties.get("key")).isSameAs(child);

    List<DeclarativeConfigProperties> fallbackList = Collections.emptyList();
    assertThat(properties.getStructuredList("key", fallbackList)).containsExactly(child);
  }
}
