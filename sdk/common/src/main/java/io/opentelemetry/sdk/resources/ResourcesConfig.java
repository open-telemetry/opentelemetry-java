/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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
   * @since 0.9.0
   */
  public static ResourcesConfig getDefault() {
    return DEFAULT;
  }

  private static final ResourcesConfig DEFAULT = ResourcesConfig.newBuilder().build();

  /**
   * Returns the fully qualified class names of {@link ResourceProvider} implementations that are
   * found on the classpath but should be disabled.
   *
   * @return the fully qualified class names of {@link ResourceProvider} implementations that are
   *     found on the classpath but should be disabled.
   */
  public abstract Set<String> getDisabledResourceProviders();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a new {@link Builder}.
   */
  public static Builder newBuilder() {
    return new AutoValue_ResourcesConfig.Builder().setDisabledResourceProviders(ImmutableSet.of());
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public abstract Builder toBuilder();

  /** Builder for {@link ResourcesConfig}. */
  @AutoValue.Builder
  public abstract static class Builder extends ConfigBuilder<Builder> {

    private static final String OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS =
        "otel.java.disabled.resource_providers";

    Builder() {}

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this
     */
    @VisibleForTesting
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);

      String stringValue = getStringProperty(OTEL_JAVA_DISABLED_RESOURCES_PROVIDERS, configMap);
      if (stringValue != null) {
        this.setDisabledResourceProviders(
            ImmutableSet.copyOf(
                Splitter.on(',').omitEmptyStrings().trimResults().split(stringValue)));
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
      Preconditions.checkArgument(
          resourcesConfig.getDisabledResourceProviders() != null, "disabledResourceProviders");
      return resourcesConfig;
    }
  }
}
