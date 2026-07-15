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
public enum ExporterTemporalityPreferenceModel {
  CUMULATIVE("cumulative"),
  DELTA("delta"),
  LOW_MEMORY("low_memory");
  private final String value;
  private static final Map<String, ExporterTemporalityPreferenceModel> CONSTANTS =
      new HashMap<String, ExporterTemporalityPreferenceModel>();

  static {
    for (ExporterTemporalityPreferenceModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExporterTemporalityPreferenceModel(String value) {
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
  public static ExporterTemporalityPreferenceModel fromValue(String value) {
    ExporterTemporalityPreferenceModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
