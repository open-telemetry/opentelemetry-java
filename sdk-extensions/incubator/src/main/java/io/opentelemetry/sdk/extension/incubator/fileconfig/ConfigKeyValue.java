/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

/** A key value pair for a YAML mapping node. */
class ConfigKeyValue {

  private final String key;
  private final DeclarativeConfigProperties value;

  private ConfigKeyValue(String key, DeclarativeConfigProperties value) {
    this.key = key;
    this.value = value;
  }

  static ConfigKeyValue of(String key, DeclarativeConfigProperties value) {
    return new ConfigKeyValue(key, value);
  }

  String getKey() {
    return key;
  }

  DeclarativeConfigProperties getValue() {
    return value;
  }
}
