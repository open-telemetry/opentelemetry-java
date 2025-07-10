/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Properties are normalized to The properties for both of these will be normalized to be all lower
 * case, dashses are replaces with periods, and environment variable underscores are replaces with
 * periods.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultConfigProperties implements ConfigProperties {

  private final Map<String, String> config;
  private final ComponentLoader componentLoader;

  /**
   * Creates a {@link DefaultConfigProperties} by merging system properties, environment variables,
   * and the {@code defaultProperties}.
   *
   * <p>Environment variables take priority over {@code defaultProperties}. System properties take
   * priority over environment variables.
   */
  public static DefaultConfigProperties create(
      Map<String, String> defaultProperties, ComponentLoader componentLoader) {
    return new DefaultConfigProperties(
        ConfigUtil.safeSystemProperties(), System.getenv(), defaultProperties, componentLoader);
  }

  /**
   * Create a {@link DefaultConfigProperties} from the {@code properties}, ignoring system
   * properties and environment variables.
   */
  public static DefaultConfigProperties createFromMap(Map<String, String> properties) {
    return new DefaultConfigProperties(
        properties,
        Collections.emptyMap(),
        Collections.emptyMap(),
        ComponentLoader.forClassLoader(DefaultConfigProperties.class.getClassLoader()));
  }

  private DefaultConfigProperties(
      Map<?, ?> systemProperties,
      Map<String, String> environmentVariables,
      Map<String, String> defaultProperties,
      ComponentLoader componentLoader) {
    Map<String, String> config = new HashMap<>();
    defaultProperties.forEach(
        (name, value) -> config.put(ConfigUtil.normalizePropertyKey(name), value));
    environmentVariables.forEach(
        (name, value) -> config.put(ConfigUtil.normalizeEnvironmentVariableKey(name), value));
    systemProperties.forEach(
        (key, value) ->
            config.put(ConfigUtil.normalizePropertyKey(key.toString()), value.toString()));

    this.config = config;
    this.componentLoader = componentLoader;
  }

  private DefaultConfigProperties(
      DefaultConfigProperties previousProperties, Map<String, String> overrides) {
    // previousProperties are already normalized, they can be copied as they are
    Map<String, String> config = new HashMap<>(previousProperties.config);
    overrides.forEach((name, value) -> config.put(ConfigUtil.normalizePropertyKey(name), value));

    this.config = config;
    this.componentLoader = previousProperties.componentLoader;
  }

  @Override
  @Nullable
  public String getString(String name) {
    return config.get(ConfigUtil.normalizePropertyKey(name));
  }

  @Override
  @Nullable
  public Boolean getBoolean(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null || value.isEmpty()) {
      return null;
    }
    return Boolean.parseBoolean(value);
  }

  @Override
  @Nullable
  @SuppressWarnings("UnusedException")
  public Integer getInt(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "integer");
    }
  }

  @Override
  @Nullable
  @SuppressWarnings("UnusedException")
  public Long getLong(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "long");
    }
  }

  @Override
  @Nullable
  @SuppressWarnings("UnusedException")
  public Double getDouble(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null || value.isEmpty()) {
      return null;
    }
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException ex) {
      throw newInvalidPropertyException(name, value, "double");
    }
  }

  @Override
  @Nullable
  @SuppressWarnings("UnusedException")
  public Duration getDuration(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null || value.isEmpty()) {
      return null;
    }
    String unitString = getUnitString(value);
    // TODO: Environment variables have unknown encoding.  `trim()` may cut codepoints oddly
    // but likely we'll fail for malformed unit string either way.
    String numberString = value.substring(0, value.length() - unitString.length());
    try {
      long rawNumber = Long.parseLong(numberString.trim());
      TimeUnit unit = getDurationUnit(unitString.trim());
      return Duration.ofNanos(TimeUnit.NANOSECONDS.convert(rawNumber, unit));
    } catch (NumberFormatException ex) {
      throw new ConfigurationException(
          "Invalid duration property "
              + name
              + "="
              + value
              + ". Expected number, found: "
              + numberString,
          ex);
    } catch (ConfigurationException ex) {
      throw new ConfigurationException(
          "Invalid duration property " + name + "=" + value + ". " + ex.getMessage());
    }
  }

  @Override
  public List<String> getList(String name) {
    String value = config.get(ConfigUtil.normalizePropertyKey(name));
    if (value == null) {
      return Collections.emptyList();
    }
    return filterBlanksAndNulls(value.split(","));
  }

  /**
   * Returns {@link ConfigProperties#getList(String)} as a {@link Set} after validating there are no
   * duplicate entries.
   *
   * @throws ConfigurationException if {@code name} contains duplicate entries
   */
  public static Set<String> getSet(ConfigProperties config, String name) {
    List<String> list = config.getList(ConfigUtil.normalizePropertyKey(name));
    Set<String> set = new HashSet<>(list);
    if (set.size() != list.size()) {
      String duplicates =
          list.stream()
              .collect(groupingBy(Function.identity(), Collectors.counting()))
              .entrySet()
              .stream()
              .filter(entry -> entry.getValue() > 1)
              .map(Map.Entry::getKey)
              .collect(joining(",", "[", "]"));
      throw new ConfigurationException(name + " contains duplicates: " + duplicates);
    }
    return set;
  }

  @Override
  public Map<String, String> getMap(String name) {
    return getList(ConfigUtil.normalizePropertyKey(name)).stream()
        .map(
            entry -> {
              String[] split = entry.split("=", 2);
              if (split.length != 2 || StringUtils.isNullOrEmpty(split[0])) {
                throw new ConfigurationException(
                    "Invalid map property: " + name + "=" + config.get(name));
              }
              return filterBlanksAndNulls(split);
            })
        // Filter entries with an empty value, i.e. "foo="
        .filter(splitKeyValuePairs -> splitKeyValuePairs.size() == 2)
        .map(
            splitKeyValuePairs ->
                new AbstractMap.SimpleImmutableEntry<>(
                    splitKeyValuePairs.get(0), splitKeyValuePairs.get(1)))
        // If duplicate keys, prioritize later ones similar to duplicate system properties on a
        // Java command line.
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (first, next) -> next, LinkedHashMap::new));
  }

  @Override
  public ComponentLoader getComponentLoader() {
    return componentLoader;
  }

  /**
   * Return a new {@link DefaultConfigProperties} by overriding the {@code previousProperties} with
   * the {@code overrides}.
   */
  public DefaultConfigProperties withOverrides(Map<String, String> overrides) {
    return new DefaultConfigProperties(this, overrides);
  }

  private static ConfigurationException newInvalidPropertyException(
      String name, String value, String type) {
    throw new ConfigurationException(
        "Invalid value for property " + name + "=" + value + ". Must be a " + type + ".");
  }

  private static List<String> filterBlanksAndNulls(String[] values) {
    return Arrays.stream(values)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
  }

  /** Returns the TimeUnit associated with a unit string. Defaults to milliseconds. */
  private static TimeUnit getDurationUnit(String unitString) {
    switch (unitString) {
      case "us":
        return TimeUnit.MICROSECONDS;
      case "ns":
        return TimeUnit.NANOSECONDS;
      case "": // Fallthrough expected
      case "ms":
        return TimeUnit.MILLISECONDS;
      case "s":
        return TimeUnit.SECONDS;
      case "m":
        return TimeUnit.MINUTES;
      case "h":
        return TimeUnit.HOURS;
      case "d":
        return TimeUnit.DAYS;
      default:
        throw new ConfigurationException("Invalid duration string, found: " + unitString);
    }
  }

  /**
   * Fragments the 'units' portion of a config value from the 'value' portion.
   *
   * <p>E.g. "1ms" would return the string "ms".
   */
  private static String getUnitString(String rawValue) {
    int lastDigitIndex = rawValue.length() - 1;
    while (lastDigitIndex >= 0) {
      char c = rawValue.charAt(lastDigitIndex);
      if (Character.isDigit(c)) {
        break;
      }
      lastDigitIndex -= 1;
    }
    // Pull everything after the last digit.
    return rawValue.substring(lastDigitIndex + 1);
  }
}
