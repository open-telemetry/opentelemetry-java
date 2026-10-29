/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public enum ExperimentalPrometheusTranslationStrategyModel {
  UNDERSCORE_ESCAPING_WITH_SUFFIXES("underscore_escaping_with_suffixes"),
  UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES_DEVELOPMENT(
      "underscore_escaping_without_suffixes/development"),
  NO_UTF_8_ESCAPING_WITH_SUFFIXES_DEVELOPMENT("no_utf8_escaping_with_suffixes/development"),
  NO_TRANSLATION_DEVELOPMENT("no_translation/development");
  private final String value;
  private static final Map<String, ExperimentalPrometheusTranslationStrategyModel> CONSTANTS =
      new HashMap<String, ExperimentalPrometheusTranslationStrategyModel>();

  static {
    for (ExperimentalPrometheusTranslationStrategyModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExperimentalPrometheusTranslationStrategyModel(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

  @JsonCreator
  public static ExperimentalPrometheusTranslationStrategyModel fromValue(String value) {
    ExperimentalPrometheusTranslationStrategyModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
