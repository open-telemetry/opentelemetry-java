/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

final class DeclarativeConfigPropertyUtil {

  private DeclarativeConfigPropertyUtil() {}

  private static final List<BiFunction<String, DeclarativeConfigProperties, Object>>
      valueResolvers =
          Arrays.asList(
              DeclarativeConfigPropertyUtil::getString,
              DeclarativeConfigPropertyUtil::getBoolean,
              DeclarativeConfigPropertyUtil::getLong,
              DeclarativeConfigPropertyUtil::getDouble,
              DeclarativeConfigPropertyUtil::getStringList,
              DeclarativeConfigPropertyUtil::getBooleanList,
              DeclarativeConfigPropertyUtil::getLongList,
              DeclarativeConfigPropertyUtil::getDoubleList,
              DeclarativeConfigPropertyUtil::getStringList,
              DeclarativeConfigPropertyUtil::getStructuredList,
              DeclarativeConfigPropertyUtil::getStructured);

  static Map<String, Object> toMap(DeclarativeConfigProperties declarativeConfigProperties) {
    Set<String> propertyKeys = declarativeConfigProperties.getPropertyKeys();
    Map<String, Object> result = new HashMap<>(propertyKeys.size());
    for (String key : declarativeConfigProperties.getPropertyKeys()) {
      result.put(key, resolveValue(key, declarativeConfigProperties));
    }
    return result;
  }

  @Nullable
  private static Object resolveValue(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    for (int i = 0; i < valueResolvers.size(); i++) {
      try {
        Object value = valueResolvers.get(i).apply(key, declarativeConfigProperties);
        if (value != null) {
          return value;
        }
      } catch (DeclarativeConfigException e) {
        // Ignore and continue
      }
    }
    return null;
  }

  @Nullable
  private static Object getString(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getString(key);
  }

  @Nullable
  private static Object getBoolean(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getBoolean(key);
  }

  @Nullable
  private static Object getLong(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getLong(key);
  }

  @Nullable
  private static Object getDouble(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getDouble(key);
  }

  @Nullable
  private static Object getStringList(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getScalarList(key, String.class);
  }

  @Nullable
  private static Object getBooleanList(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getScalarList(key, Boolean.class);
  }

  @Nullable
  private static Object getLongList(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getScalarList(key, Long.class);
  }

  @Nullable
  private static Object getDoubleList(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return declarativeConfigProperties.getScalarList(key, Double.class);
  }

  @Nullable
  private static Object getStructuredList(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return Optional.ofNullable(declarativeConfigProperties.getStructuredList(key))
        .map(list -> list.stream().map(DeclarativeConfigPropertyUtil::toMap).collect(toList()))
        .orElse(null);
  }

  @Nullable
  private static Object getStructured(
      String key, DeclarativeConfigProperties declarativeConfigProperties) {
    return Optional.ofNullable(declarativeConfigProperties.getStructured(key))
        .map(DeclarativeConfigPropertyUtil::toMap)
        .orElse(null);
  }
}
