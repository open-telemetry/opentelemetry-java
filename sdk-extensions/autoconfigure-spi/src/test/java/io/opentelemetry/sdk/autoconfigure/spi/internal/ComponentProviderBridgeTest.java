/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.config.DeclarativeConfigProperties;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class ComponentProviderBridgeTest {

  @Test
  void testLegacyProviderWithPureModernConfig() {
    ComponentProvider legacyProvider =
        new ComponentProvider() {
          @Override
          public Class<String> getType() {
            return String.class;
          }

          @Override
          public String getName() {
            return "legacy";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            return "created:" + config.getString("key");
          }
        };

    DeclarativeConfigProperties modernConfig = mock(DeclarativeConfigProperties.class);
    when(modernConfig.getString("key")).thenReturn("value");

    Object result = legacyProvider.create(modernConfig);
    assertThat(result).isEqualTo("created:value");
  }

  @Test
  void testLegacyProviderWithIncubatorConfig() {
    ComponentProvider legacyProvider =
        new ComponentProvider() {
          @Override
          public Class<String> getType() {
            return String.class;
          }

          @Override
          public String getName() {
            return "legacy";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            return "created:" + config.getString("key");
          }
        };

    io.opentelemetry.api.incubator.config.DeclarativeConfigProperties incubatorConfig =
        mock(io.opentelemetry.api.incubator.config.DeclarativeConfigProperties.class);
    when(incubatorConfig.getString("key")).thenReturn("incubatorValue");

    // Invoke via modern signature where config is an instance of incubator config
    DeclarativeConfigProperties polyConfig = incubatorConfig;
    Object result = legacyProvider.create(polyConfig);
    assertThat(result).isEqualTo("created:incubatorValue");
  }

  @Test
  void testModernProviderWithIncubatorConfig() {
    ComponentProvider modernProvider =
        new ComponentProvider() {
          @Override
          public Class<String> getType() {
            return String.class;
          }

          @Override
          public String getName() {
            return "modern";
          }

          @Override
          public Object create(DeclarativeConfigProperties config) {
            return "created:" + config.getString("key");
          }
        };

    io.opentelemetry.api.incubator.config.DeclarativeConfigProperties incubatorConfig =
        mock(io.opentelemetry.api.incubator.config.DeclarativeConfigProperties.class);
    when(incubatorConfig.getString("key")).thenReturn("legacyValue");

    // Invoke via deprecated legacy signature
    Object result = modernProvider.create(incubatorConfig);
    assertThat(result).isEqualTo("created:legacyValue");
  }
}
