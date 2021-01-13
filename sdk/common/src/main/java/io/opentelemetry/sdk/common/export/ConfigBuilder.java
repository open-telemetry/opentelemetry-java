/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base class for all the config builder classes (SimpleSpanProcessor, BatchSpanProcessor, etc).
 *
 * <p>The type parameter on this class should be set to the type of the implementation of this
 * abstract class. For example, if your subclass is a Foo.Builder, then you would declare it as
 *
 * <p>{@code Foo.Builder extends ConfigBuilder<Foo.Builder>}
 *
 * @param <T> the type of the implementation.
 * @deprecated Define an implementation of {@code SdkTracerProviderConfigurer} from the
 *     opentelemetry-sdk-extension-autoconfigure module to allow autoconfiguration of your type.
 */
@Deprecated
public abstract class ConfigBuilder<T> {

  // Visible for testing
  protected enum NamingConvention {
    DOT {
      @Override
      public String normalize(@Nonnull String key) {
        return key.toLowerCase();
      }
    },
    ENV_VAR {
      @Override
      public String normalize(@Nonnull String key) {
        return key.toLowerCase().replace("_", ".");
      }
    };

    public abstract String normalize(@Nonnull String key);

    /**
     * Normalize the key value of the map using the class {@link #normalize(String)} method.
     *
     * @see #normalize(String)
     * @param map The map to normalize
     * @return an unmodifiable normalized map
     */
    public Map<String, String> normalize(@Nonnull Map<String, String> map) {
      Map<String, String> properties = new HashMap<>();
      for (Map.Entry<String, String> entry : map.entrySet()) {
        properties.put(normalize(entry.getKey()), entry.getValue());
      }
      return Collections.unmodifiableMap(properties);
    }
  }

  /** Sets the configuration values from the given configuration map for only the available keys. */
  protected abstract T fromConfigMap(
      Map<String, String> configMap, NamingConvention namingConvention);

  /** Sets the configuration values from the given {@link Properties} object. */
  public T readProperties(Properties properties) {
    // Properties incorrectly implements Map<Object, Object> but we know it only has Strings.
    @SuppressWarnings({"unchecked", "rawtypes"})
    Map<String, String> map = (Map) properties;
    return fromConfigMap(map, NamingConvention.DOT);
  }

  /** Sets the configuration values from environment variables. */
  public T readEnvironmentVariables() {
    return fromConfigMap(System.getenv(), NamingConvention.ENV_VAR);
  }

  /** Sets the configuration values from system properties. */
  public T readSystemProperties() {
    return readProperties(System.getProperties());
  }

  /**
   * Get a boolean property from the map, {@code null} if it cannot be found or it has a wrong type.
   *
   * @param name The property name
   * @param map The map where to look for the property
   * @return the {@link Boolean} value of the property, {@code null} in case of error or if the
   *     property cannot be found.
   */
  @Nullable
  protected static Boolean getBooleanProperty(String name, Map<String, String> map) {
    if (map.containsKey(name)) {
      return Boolean.parseBoolean(map.get(name));
    }
    return null;
  }

  /**
   * Get an integer property from the map, {@code null} if it cannot be found or it has a wrong
   * type.
   *
   * @param name The property name
   * @param map The map where to look for the property
   * @return the {@link Integer} value of the property, {@code null} in case of error or if the
   *     property cannot be found.
   */
  @Nullable
  protected static Integer getIntProperty(String name, Map<String, String> map) {
    try {
      return Integer.parseInt(map.get(name));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Get a long property from the map, {@code null} if it cannot be found or it has a wrong type.
   *
   * @param name The property name
   * @param map The map where to look for the property
   * @return the {@link Long} value of the property, {@code null} in case of error or if the
   *     property cannot be found.
   */
  @Nullable
  protected static Long getLongProperty(String name, Map<String, String> map) {
    try {
      return Long.parseLong(map.get(name));
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  /**
   * Get a {@link String} property from the map, {@code null} if it cannot be found.
   *
   * @param name The property name
   * @param map The map where to look for the property
   * @return the {@link String} value of the property, {@code null} if the property cannot be found.
   */
  @Nullable
  protected static String getStringProperty(String name, Map<String, String> map) {
    return map.get(name);
  }

  /**
   * Get a double property from the map, {@code null} if it cannot be found or it has a wrong type.
   *
   * @param name The property name
   * @param map The map where to look for the property
   * @return the {@link Double} value of the property, {@code null} in case of error or if the
   *     property cannot be found.
   */
  @Nullable
  protected static Double getDoubleProperty(String name, Map<String, String> map) {
    try {
      return Double.parseDouble(map.get(name));
    } catch (NumberFormatException | NullPointerException ex) {
      return null;
    }
  }
}
