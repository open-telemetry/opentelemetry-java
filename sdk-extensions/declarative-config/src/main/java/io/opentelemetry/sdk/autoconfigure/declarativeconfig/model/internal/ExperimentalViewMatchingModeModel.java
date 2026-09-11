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
public enum ExperimentalViewMatchingModeModel {
  INDEPENDENT("independent"),
  COMPOSABLE("composable");
  private final String value;
  private static final Map<String, ExperimentalViewMatchingModeModel> CONSTANTS =
      new HashMap<String, ExperimentalViewMatchingModeModel>();

  static {
    for (ExperimentalViewMatchingModeModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExperimentalViewMatchingModeModel(String value) {
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
  public static ExperimentalViewMatchingModeModel fromValue(String value) {
    ExperimentalViewMatchingModeModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
