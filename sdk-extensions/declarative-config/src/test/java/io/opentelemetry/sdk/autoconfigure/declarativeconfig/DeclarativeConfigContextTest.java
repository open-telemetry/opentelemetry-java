/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.config.DeclarativeConfigException;
import io.opentelemetry.api.config.DeclarativeConfigProperties;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.declarativeconfig.ComponentProvider;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
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
  @SuppressWarnings("deprecation")
  void componentProvidersCached() {
    // First loadComponent call should load providers
    assertThatThrownBy(() ->
            context.loadComponent(
                Resource.class,
                ConfigKeyValue.of(
                    "nonexistent",
                    YamlDeclarativeConfigProperties.create(
                        Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("No component provider detected");

    // Second loadComponent call should use cached providers, not reload
    assertThatThrownBy(() ->
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
    verify(componentLoader, times(1))
        .load(io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider.class);
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

  @Test
  @SuppressWarnings("deprecation")
  void legacyComponentProviderLoaded() {
    Resource expectedResource = Resource.builder().put("source", "legacy").build();
    io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider legacyProvider =
        new io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "legacy-test";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            assertThat(config).isInstanceOf(LegacyYamlDeclarativeConfigProperties.class);
            return expectedResource;
          }
        };

    doReturn(Collections.singletonList(legacyProvider))
        .when(componentLoader)
        .load(io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider.class);

    Resource resource =
        context.loadComponent(
            Resource.class,
            ConfigKeyValue.of(
                "legacy-test",
                YamlDeclarativeConfigProperties.create(Collections.emptyMap(), componentLoader)));

    assertThat(resource).isSameAs(expectedResource);
  }

  @Test
  @SuppressWarnings("deprecation")
  void legacyComponentProviderConflictWhenModernProviderExists() {
    Resource modernResource = Resource.builder().put("source", "modern").build();
    Resource legacyResource = Resource.builder().put("source", "legacy").build();

    ComponentProvider modernProvider =
        new ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "duplicate-test";
          }

          @Override
          public Object create(DeclarativeConfigProperties config) {
            return modernResource;
          }
        };

    io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider legacyProvider =
        new io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "duplicate-test";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            return legacyResource;
          }
        };

    doReturn(Collections.singletonList(modernProvider))
        .when(componentLoader)
        .load(ComponentProvider.class);
    doReturn(Collections.singletonList(legacyProvider))
        .when(componentLoader)
        .load(io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider.class);

    assertThatThrownBy(() ->
            context.loadComponent(
                Resource.class,
                ConfigKeyValue.of(
                    "duplicate-test",
                    YamlDeclarativeConfigProperties.create(
                        Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("Component provider conflict. Multiple providers detected for");
  }

  @Test
  @SuppressWarnings("deprecation")
  void legacyComponentProviderConflictWhenMultipleLegacyProvidersExist() {
    Resource legacyResource1 = Resource.builder().put("source", "legacy1").build();
    Resource legacyResource2 = Resource.builder().put("source", "legacy2").build();

    io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider legacyProvider1 =
        new io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "duplicate-legacy";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            return legacyResource1;
          }
        };

    io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider legacyProvider2 =
        new io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "duplicate-legacy";
          }

          @Override
          public Object create(
              io.opentelemetry.api.incubator.config.DeclarativeConfigProperties config) {
            return legacyResource2;
          }
        };

    doReturn(Arrays.asList(legacyProvider1, legacyProvider2))
        .when(componentLoader)
        .load(io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider.class);

    assertThatThrownBy(() ->
            context.loadComponent(
                Resource.class,
                ConfigKeyValue.of(
                    "duplicate-legacy",
                    YamlDeclarativeConfigProperties.create(
                        Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("Component provider conflict. Multiple providers detected for");
  }

  @Test
  void componentProviderUnexpectedComponentType() {
    ComponentProvider provider =
        new ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "bad-type";
          }

          @Override
          public Object create(DeclarativeConfigProperties config) {
            return "not-a-resource";
          }
        };

    doReturn(Collections.singletonList(provider))
        .when(componentLoader)
        .load(ComponentProvider.class);

    assertThatThrownBy(() ->
            context.loadComponent(
                Resource.class,
                ConfigKeyValue.of(
                    "bad-type",
                    YamlDeclarativeConfigProperties.create(
                        Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageStartingWith(
            "Error configuring io.opentelemetry.sdk.resources.Resource with name \"bad-type\"")
        .hasCauseInstanceOf(DeclarativeConfigException.class)
        .hasRootCauseMessage(
            "Error configuring io.opentelemetry.sdk.resources.Resource with name \"bad-type\". Component provider "
                + provider.getClass().getName()
                + " returned an unexpected component type: java.lang.String");
  }

  @Test
  void componentProviderThrowsExceptionWrapped() {
    ComponentProvider provider =
        new ComponentProvider() {
          @Override
          public Class<?> getType() {
            return Resource.class;
          }

          @Override
          public String getName() {
            return "throws-error";
          }

          @Override
          public Object create(DeclarativeConfigProperties config) {
            throw new IllegalStateException("initialization failure");
          }
        };

    doReturn(Collections.singletonList(provider))
        .when(componentLoader)
        .load(ComponentProvider.class);

    assertThatThrownBy(() ->
            context.loadComponent(
                Resource.class,
                ConfigKeyValue.of(
                    "throws-error",
                    YamlDeclarativeConfigProperties.create(
                        Collections.emptyMap(), componentLoader))))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageStartingWith(
            "Error configuring io.opentelemetry.sdk.resources.Resource with name \"throws-error\"")
        .hasCauseInstanceOf(IllegalStateException.class);
  }

  @Test
  void setInternalTelemetry_nullWhenConfigProviderNull() {
    AtomicReference<MeterProvider> meterProviderRef = new AtomicReference<>();
    AtomicReference<InternalTelemetryVersion> versionRef = new AtomicReference<>();

    context.setInternalTelemetry(supplier -> meterProviderRef.set(supplier.get()), versionRef::set);

    assertThat(meterProviderRef.get()).isSameAs(MeterProvider.noop());
    assertThat(versionRef.get()).isNull();
  }

  @Test
  void setInternalTelemetry_legacy() {
    ConfigProvider configProvider = mock(ConfigProvider.class);
    DeclarativeConfigProperties otelSdkProps = mock(DeclarativeConfigProperties.class);
    when(configProvider.getInstrumentationConfig("otel_sdk")).thenReturn(otelSdkProps);
    when(otelSdkProps.getString("internal_telemetry_version")).thenReturn("legacy");

    DeclarativeConfigContext ctx = new DeclarativeConfigContext(componentLoader);
    ctx.setConfigProvider(configProvider);

    AtomicReference<InternalTelemetryVersion> versionRef = new AtomicReference<>();
    ctx.setInternalTelemetry(supplier -> {}, versionRef::set);

    assertThat(versionRef.get()).isEqualTo(InternalTelemetryVersion.LEGACY);
  }

  @Test
  void setInternalTelemetry_invalid() {
    ConfigProvider configProvider = mock(ConfigProvider.class);
    DeclarativeConfigProperties otelSdkProps = mock(DeclarativeConfigProperties.class);
    when(configProvider.getInstrumentationConfig("otel_sdk")).thenReturn(otelSdkProps);
    when(otelSdkProps.getString("internal_telemetry_version")).thenReturn("invalid_ver");

    DeclarativeConfigContext ctx = new DeclarativeConfigContext(componentLoader);
    ctx.setConfigProvider(configProvider);

    assertThatThrownBy(() -> ctx.setInternalTelemetry(supplier -> {}, version -> {}))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("Invalid sdk telemetry version: invalid_ver");
  }
}
