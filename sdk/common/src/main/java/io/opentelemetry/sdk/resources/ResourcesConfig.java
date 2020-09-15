/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.resources;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
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
 *   <li>{@code otel.java.disable.resources.providers}: to set the ResourceProvider service
 *       providers found on the classpath to be disabled.
 * </ul>
 *
 * <p>For environment variables, {@link ResourcesConfig} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_JAVA_DISABLE_RESOURCES_PROVIDERS}: to set the ResourceProvider service
 *       providers found on the classpath to be disabled.
 * </ul>
 */
@AutoValue
@Immutable
public abstract class ResourcesConfig {
  // These values are the default values for all the global parameters.
  private static final ImmutableSet<String> OTEL_JAVA_DISABLE_RESOURCES_PROVIDERS =
      ImmutableSet.of();

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
   * Returns the ResourceProvider service providers found on the classpath to be disabled.
   *
   * @return the ResourceProvider service providers found on the classpath to be disabled.
   */
  public abstract Set<String> getDisabledResourceProviders();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a new {@link Builder}.
   */
  public static Builder newBuilder() {
    return new AutoValue_ResourcesConfig.Builder()
        .setDisabledResourceProviders(OTEL_JAVA_DISABLE_RESOURCES_PROVIDERS);
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

    private static final String OTEL_JAVA_DISABLE_RESOURCES_PROVIDERS =
        "otel.java.disable.resources.providers";

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

      String stringValue = getStringProperty(OTEL_JAVA_DISABLE_RESOURCES_PROVIDERS, configMap);
      if (stringValue != null) {
        this.setDisabledResourceProviders(ImmutableSet.copyOf(stringValue.split(",")));
      }
      return this;
    }

    /**
     * @param disabledResourceProviders the ResourceProvider service providers found on the
     *     classpath to be disabled.
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
