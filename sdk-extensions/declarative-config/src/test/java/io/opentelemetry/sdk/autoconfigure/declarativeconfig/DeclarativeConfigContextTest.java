/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DeclarativeConfigContextTest {

  private final ComponentLoader componentLoader =
      spy(ComponentLoader.forClassLoader(getClass().getClassLoader()));
  private final DeclarativeConfigContext context = new DeclarativeConfigContext(componentLoader);

  @Test
  void componentProvidersCached() {
    // First loadComponent call should load providers
    assertThatThrownBy(
            () ->
                context.loadComponent(
                    Resource.class,
                    ConfigKeyValue.of(
                        "nonexistent",
                        YamlDeclarativeConfigProperties.create(
                            Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("No component provider detected");

    // Second loadComponent call should use cached providers, not reload
    assertThatThrownBy(
            () ->
                context.loadComponent(
                    Resource.class,
                    ConfigKeyValue.of(
                        "another",
                        YamlDeclarativeConfigProperties.create(
                            Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("No component provider detected");

    // Verify spiHelper.load() was only called once
    verify(componentLoader, times(1)).load(ComponentProvider.class);
  }

  @Test
  void componentConfigForwardsTypeIntrospection() {
    AtomicReference<DeclarativeConfigProperties> received = new AtomicReference<>();
    ComponentProvider provider =
        new ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "test";
          }

          @Override
          public Object create(DeclarativeConfigProperties config) {
            received.set(config);
            return Resource.empty();
          }
        };
    doReturn(Collections.singletonList(provider))
        .when(componentLoader)
        .load(ComponentProvider.class);

    Map<String, Object> properties = new HashMap<>();
    properties.put("str_key", "value");
    properties.put("bool_key", true);
    properties.put("int_key", 1);
    properties.put("double_key", 1.5);
    context.loadComponent(
        Resource.class,
        ConfigKeyValue.of(
            "test", YamlDeclarativeConfigProperties.create(properties, componentLoader)));

    DeclarativeConfigProperties config = received.get();
    assertThat(config).isNotNull();
    assertThat(config.isString("str_key")).isTrue();
    assertThat(config.isBoolean("bool_key")).isTrue();
    assertThat(config.isInt("int_key")).isTrue();
    assertThat(config.isLong("int_key")).isTrue();
    assertThat(config.isDouble("double_key")).isTrue();
    assertThat(config.isString("int_key")).isFalse();
  }
}
