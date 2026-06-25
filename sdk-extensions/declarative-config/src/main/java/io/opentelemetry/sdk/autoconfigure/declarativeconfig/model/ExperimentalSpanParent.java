/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;

@Generated("jsonschema2pojo")
public enum ExperimentalSpanParent {
  NONE("none"),
  REMOTE("remote"),
  LOCAL("local");
  private final String value;
  private static final Map<String, ExperimentalSpanParent> CONSTANTS =
      new HashMap<String, ExperimentalSpanParent>();

  static {
    for (ExperimentalSpanParent c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExperimentalSpanParent(String value) {
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
  public static ExperimentalSpanParent fromValue(String value) {
    ExperimentalSpanParent constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
