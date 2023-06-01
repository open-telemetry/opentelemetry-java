/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Properties used for auto-configuration of the OpenTelemetry SDK components. */
public interface ConfigProperties extends io.opentelemetry.sdk.common.config.ConfigProperties {

  @Override
  @Nullable
  String getString(String name);

  @Override
  default String getString(String name, String defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getString(name, defaultValue);
  }

  @Override
  @Nullable
  Boolean getBoolean(String name);

  @Override
  default boolean getBoolean(String name, boolean defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getBoolean(name, defaultValue);
  }

  @Override
  @Nullable
  Integer getInt(String name);

  @Override
  default int getInt(String name, int defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getInt(name, defaultValue);
  }

  @Override
  @Nullable
  Long getLong(String name);

  @Override
  default long getLong(String name, long defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getLong(name, defaultValue);
  }

  @Override
  @Nullable
  Double getDouble(String name);

  @Override
  default double getDouble(String name, double defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getDouble(name, defaultValue);
  }

  @Override
  @Nullable
  Duration getDuration(String name);

  @Override
  default Duration getDuration(String name, Duration defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getDuration(
        name, defaultValue);
  }

  @Override
  List<String> getList(String name);

  @Override
  default List<String> getList(String name, List<String> defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getList(name, defaultValue);
  }

  @Override
  Map<String, String> getMap(String name);

  @Override
  default Map<String, String> getMap(String name, Map<String, String> defaultValue) {
    return io.opentelemetry.sdk.common.config.ConfigProperties.super.getMap(name, defaultValue);
  }
}
