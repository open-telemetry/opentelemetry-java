/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  /** Add the {@code closeable} to the {@code closeables} and return it. */
  static <T> T addAndReturn(List<Closeable> closeables, T closeable) {
    if (closeable instanceof Closeable) {
      closeables.add((Closeable) closeable);
    }
    return closeable;
  }

  static <T> T assertNotNull(@Nullable T object, String description) {
    if (object == null) {
      throw new NullPointerException(description + " is null");
    }
    return object;
  }

  static <T> T requireNonNull(@Nullable T object, String description) {
    if (object == null) {
      throw new DeclarativeConfigException(description + " is required but is null");
    }
    return object;
  }

  /**
   * Find a registered {@link ComponentProvider} which {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(DeclarativeConfigProperties)} with the given {@code model}.
   *
   * @throws DeclarativeConfigException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(DeclarativeConfigProperties)} throws
   */
  static <T> T loadComponent(SpiHelper spiHelper, Class<T> type, String name, Object model) {
    // Map model to generic structured config properties
    DeclarativeConfigProperties config =
        DeclarativeConfiguration.toConfigProperties(model, spiHelper.getComponentLoader());
    return loadComponentHelper(spiHelper, type, name, config);
  }

  /**
   * Find a registered {@link ComponentProvider} with {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(DeclarativeConfigProperties)} with the given {@code config}.
   *
   * @throws DeclarativeConfigException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(DeclarativeConfigProperties)} throws
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static <T> T loadComponentHelper(
      SpiHelper spiHelper, Class<T> type, String name, DeclarativeConfigProperties config) {
    // TODO(jack-berg): cache loaded component providers
    List<ComponentProvider> componentProviders = spiHelper.load(ComponentProvider.class);
    List<ComponentProvider<?>> matchedProviders =
        componentProviders.stream()
            .map(
                (Function<ComponentProvider, ComponentProvider<?>>)
                    componentProvider -> componentProvider)
            .filter(
                componentProvider ->
                    componentProvider.getType() == type && name.equals(componentProvider.getName()))
            .collect(Collectors.toList());
    if (matchedProviders.isEmpty()) {
      throw new DeclarativeConfigException(
          "No component provider detected for " + type.getName() + " with name \"" + name + "\".");
    }
    if (matchedProviders.size() > 1) {
      throw new DeclarativeConfigException(
          "Component provider conflict. Multiple providers detected for "
              + type.getName()
              + " with name \""
              + name
              + "\": "
              + componentProviders.stream()
                  .map(provider -> provider.getClass().getName())
                  .collect(Collectors.joining(",", "[", "]")));
    }
    // Exactly one matching component provider
    ComponentProvider<T> provider = (ComponentProvider<T>) matchedProviders.get(0);

    try {
      return provider.create(config);
    } catch (Throwable throwable) {
      throw new DeclarativeConfigException(
          "Error configuring " + type.getName() + " with name \"" + name + "\"", throwable);
    }
  }

  static Map.Entry<String, Object> getSingletonMapEntry(
      Map<String, Object> additionalProperties, String resourceName) {
    if (additionalProperties.isEmpty()) {
      throw new DeclarativeConfigException(resourceName + " must be set");
    }
    if (additionalProperties.size() > 1) {
      throw new DeclarativeConfigException(
          "Invalid configuration - multiple "
              + resourceName
              + "s set: "
              + additionalProperties.keySet().stream().collect(joining(",", "[", "]")));
    }
    return additionalProperties.entrySet().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Missing " + resourceName + ". This is a programming error."));
  }
}
