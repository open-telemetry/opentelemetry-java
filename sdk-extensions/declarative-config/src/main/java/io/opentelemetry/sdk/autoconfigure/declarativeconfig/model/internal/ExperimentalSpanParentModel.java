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
public enum ExperimentalSpanParentModel {
  NONE("none"),
  REMOTE("remote"),
  LOCAL("local");
  private final String value;
  private static final Map<String, ExperimentalSpanParentModel> CONSTANTS =
      new HashMap<String, ExperimentalSpanParentModel>();

  static {
    for (ExperimentalSpanParentModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExperimentalSpanParentModel(String value) {
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
  public static ExperimentalSpanParentModel fromValue(String value) {
    ExperimentalSpanParentModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
