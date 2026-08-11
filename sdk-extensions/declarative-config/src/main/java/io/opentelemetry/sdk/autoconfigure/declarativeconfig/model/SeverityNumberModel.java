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
public enum SeverityNumberModel {
  TRACE("trace"),
  TRACE_2("trace2"),
  TRACE_3("trace3"),
  TRACE_4("trace4"),
  DEBUG("debug"),
  DEBUG_2("debug2"),
  DEBUG_3("debug3"),
  DEBUG_4("debug4"),
  INFO("info"),
  INFO_2("info2"),
  INFO_3("info3"),
  INFO_4("info4"),
  WARN("warn"),
  WARN_2("warn2"),
  WARN_3("warn3"),
  WARN_4("warn4"),
  ERROR("error"),
  ERROR_2("error2"),
  ERROR_3("error3"),
  ERROR_4("error4"),
  FATAL("fatal"),
  FATAL_2("fatal2"),
  FATAL_3("fatal3"),
  FATAL_4("fatal4");
  private final String value;
  private static final Map<String, SeverityNumberModel> CONSTANTS =
      new HashMap<String, SeverityNumberModel>();

  static {
    for (SeverityNumberModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  SeverityNumberModel(String value) {
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
  public static SeverityNumberModel fromValue(String value) {
    SeverityNumberModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
