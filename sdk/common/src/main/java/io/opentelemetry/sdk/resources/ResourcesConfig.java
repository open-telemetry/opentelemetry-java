/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds global resources parameters.
 *
 * <p>Configuration options for {@link ResourcesConfig} can be read from system properties,
 * environment variables, or {@link Properties} objects.
 *
 * <p>For system properties and {@link Properties} objects, {@link ResourcesConfig} will look for
 * the following names:
 *
 * <ul>
 *   <li>{@code otel.java.disabled.resource_providers}: to set the fully qualified class names of
 *       {@link ResourceProvider} implementations that are found on the classpath but should be
 *       disabled.
 * </ul>
 *
 * <p>For environment variables, {@link ResourcesConfig} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS}: to set the fully qualified class names of
 *       {@link ResourceProvider} implementations that are found on the classpath but should be
 *       disabled.
 * </ul>
 */
@AutoValue
@Immutable
public abstract class ResourcesConfig {

  /**
   * Returns the default {@code ResourcesConfig}.
   *
   * @return the default {@code ResourcesConfig}.
   */
  public static ResourcesConfig getDefault() {
    return DEFAULT;
  }

  private static final ResourcesConfig DEFAULT = ResourcesConfig.builder().build();

  /**
   * Returns the fully qualified class names of {@link ResourceProvider} implementations that are
   * found on the classpath but should be disabled.
   *
   * @return the fully qualified class names of {@link ResourceProvider} implementations that are
   *     found on the classpath but should be disabled.
   */
  public abstract Set<String> getDisabledResourceProviders();

  static Builder builder() {
    return new AutoValue_ResourcesConfig.Builder()
        .setDisabledResourceProviders(Collections.emptySet());
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public abstract Builder toBuilder();

  /** Builder for {@link ResourcesConfig}. */
  @AutoValue.Builder
  abstract static class Builder extends ConfigBuilder<Builder> {

    private static final String OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS =
        "otel.java.disabled.resource_providers";

    Builder() {}

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this
     */
    // Visible for testing
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);

      String stringValue = getStringProperty(OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS, configMap);
      if (stringValue != null) {
        this.setDisabledResourceProviders(
            Collections.unmodifiableSet(
                Arrays.stream(stringValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet())));
      }
      return this;
    }

    /**
     * Sets the fully qualified class names of {@link ResourceProvider} implementations that are
     * found on the classpath but should be disabled.
     *
     * @param disabledResourceProviders the fully qualified class names of {@link ResourceProvider}
     *     implementations that are found on the classpath but should be disabled.
     * @return this.
     */
    public abstract Builder setDisabledResourceProviders(Set<String> disabledResourceProviders);

    abstract ResourcesConfig autoBuild();

    /**
     * Builds and returns a {@code ResourcesConfig} with the desired values.
     *
     * @return a {@code ResourcesConfig} with the desired values.
     */
    public ResourcesConfig build() {
      ResourcesConfig resourcesConfig = autoBuild();
      requireNonNull(resourcesConfig.getDisabledResourceProviders(), "disabledResourceProviders");
      return resourcesConfig;
    }
  }
}
