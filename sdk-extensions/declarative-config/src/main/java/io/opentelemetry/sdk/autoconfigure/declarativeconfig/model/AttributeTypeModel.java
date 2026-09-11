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
public enum AttributeTypeModel {
  STRING("string"),
  BOOL("bool"),
  INT("int"),
  DOUBLE("double"),
  STRING_ARRAY("string_array"),
  BOOL_ARRAY("bool_array"),
  INT_ARRAY("int_array"),
  DOUBLE_ARRAY("double_array");
  private final String value;
  private static final Map<String, AttributeTypeModel> CONSTANTS =
      new HashMap<String, AttributeTypeModel>();

  static {
    for (AttributeTypeModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  AttributeTypeModel(String value) {
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
  public static AttributeTypeModel fromValue(String value) {
    AttributeTypeModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
