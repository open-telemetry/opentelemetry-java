/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.internal.YamlDeclarativeConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
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
}
