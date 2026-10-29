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
public enum InstrumentTypeModel {
  COUNTER("counter"),
  GAUGE("gauge"),
  HISTOGRAM("histogram"),
  OBSERVABLE_COUNTER("observable_counter"),
  OBSERVABLE_GAUGE("observable_gauge"),
  OBSERVABLE_UP_DOWN_COUNTER("observable_up_down_counter"),
  UP_DOWN_COUNTER("up_down_counter");
  private final String value;
  private static final Map<String, InstrumentTypeModel> CONSTANTS =
      new HashMap<String, InstrumentTypeModel>();

  static {
    for (InstrumentTypeModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  InstrumentTypeModel(String value) {
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
  public static InstrumentTypeModel fromValue(String value) {
    InstrumentTypeModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
