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

@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public enum ExemplarFilterModel {
  ALWAYS_ON("always_on"),
  ALWAYS_OFF("always_off"),
  TRACE_BASED("trace_based");
  private final String value;
  private static final Map<String, ExemplarFilterModel> CONSTANTS =
      new HashMap<String, ExemplarFilterModel>();

  static {
    for (ExemplarFilterModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExemplarFilterModel(String value) {
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
  public static ExemplarFilterModel fromValue(String value) {
    ExemplarFilterModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
