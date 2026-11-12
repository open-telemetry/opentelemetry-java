/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.common.impl.ConfigUtil;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationMapper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DeclarativeConfigurationYamlParser;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeclarativeConfigProviderLoader {

  private static final Logger LOGGER =
      Logger.getLogger(DeclarativeConfigProviderLoader.class.getName());

  // TODO(jack-berg): Allow callers to supply the ComponentLoader used to resolve parser and mapper
  // providers.
  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());

  private static final AtomicReference<DeclarativeConfigurationMapper> MAPPER =
      new AtomicReference<>();
  private static final AtomicReference<DeclarativeConfigurationYamlParser> YAML_PARSER =
      new AtomicReference<>();

  private DeclarativeConfigProviderLoader() {}

  /** Returns the cached model mapper, resolving it on first use. */
  public static DeclarativeConfigurationMapper getMapper() {
    return resolveProvider(
        MAPPER,
        DeclarativeConfigurationMapper.class,
        COMPONENT_LOADER,
        "No DeclarativeConfigurationMapper provider is available. Add the optional "
            + "opentelemetry-sdk-extension-declarative-config-jackson2 artifact or register a "
            + "custom mapper provider.");
  }

  /** Returns the cached YAML parser, resolving it on first use. */
  public static DeclarativeConfigurationYamlParser getYamlParser() {
    return resolveProvider(
        YAML_PARSER,
        DeclarativeConfigurationYamlParser.class,
        COMPONENT_LOADER,
        "No DeclarativeConfigurationYamlParser provider is available. Add the optional "
            + "opentelemetry-sdk-extension-declarative-config-snakeyaml-engine2 artifact or register a "
            + "custom parser provider.");
  }

  static <T> T resolveProvider(
      AtomicReference<T> providerRef,
      Class<T> spiClass,
      ComponentLoader componentLoader,
      String missingProviderMessage) {
    T provider = providerRef.get();
    if (provider != null) {
      return provider;
    }
    String configProperty = spiClass.getName();
    T candidate =
        loadFirstOrConfigured(
            spiClass,
            missingProviderMessage,
            configProperty,
            componentLoader,
            ConfigUtil.getString(configProperty, ""));
    return providerRef.compareAndSet(null, candidate)
        ? candidate
        : requireNonNull(providerRef.get());
  }

  private static <T> T loadFirstOrConfigured(
      Class<T> spiClass,
      String missingProviderMessage,
      String configProperty,
      ComponentLoader componentLoader,
      String configuredProvider) {
    List<T> providers = ComponentLoader.loadList(componentLoader, spiClass);
    if (providers.isEmpty()) {
      throw new DeclarativeConfigException(missingProviderMessage);
    }
    if (providers.size() == 1) {
      return providers.get(0);
    }

    if (configuredProvider.isEmpty()) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
            Level.WARNING,
            "Multiple {0} providers found. Please include only one, or specify preference "
                + "setting {1} to the FQCN of the preferred provider.",
            new Object[] {spiClass.getSimpleName(), configProperty});
      }
      return providers.get(0);
    }

    for (T provider : providers) {
      if (provider.getClass().getName().equals(configuredProvider)) {
        return provider;
      }
    }
    throw new IllegalStateException(
        "No "
            + spiClass.getSimpleName()
            + " matched configured "
            + configProperty
            + ": "
            + configuredProvider);
  }
}
