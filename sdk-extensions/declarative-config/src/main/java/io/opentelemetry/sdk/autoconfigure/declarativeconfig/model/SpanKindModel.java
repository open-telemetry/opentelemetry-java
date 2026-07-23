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
public enum SpanKindModel {
  INTERNAL("internal"),
  SERVER("server"),
  CLIENT("client"),
  PRODUCER("producer"),
  CONSUMER("consumer");
  private final String value;
  private static final Map<String, SpanKindModel> CONSTANTS = new HashMap<String, SpanKindModel>();

  static {
    for (SpanKindModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  SpanKindModel(String value) {
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
  public static SpanKindModel fromValue(String value) {
    SpanKindModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
