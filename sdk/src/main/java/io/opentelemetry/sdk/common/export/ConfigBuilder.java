/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.common.export;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConfigBuilder<T> {

  @VisibleForTesting
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

  protected abstract T fromConfigMap(
      Map<String, String> configMap, NamingConvention namingConvention);

  public abstract T readProperties(Properties properties);

  public abstract T readEnvironment();

  public abstract T readSystemProperties();

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
   * @return the {@link Long} value of the property, {@code null} in case of error or if the property
   *     cannot be found.
   */
  @Nullable
  protected static Long getLongProperty(String name, Map<String, String> map) {
    try {
      return Long.parseLong(map.get(name));
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
