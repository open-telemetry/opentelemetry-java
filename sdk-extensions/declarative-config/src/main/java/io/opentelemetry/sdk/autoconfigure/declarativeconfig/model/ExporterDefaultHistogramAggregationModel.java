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
public enum ExporterDefaultHistogramAggregationModel {
  EXPLICIT_BUCKET_HISTOGRAM("explicit_bucket_histogram"),
  BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM("base2_exponential_bucket_histogram");
  private final String value;
  private static final Map<String, ExporterDefaultHistogramAggregationModel> CONSTANTS =
      new HashMap<String, ExporterDefaultHistogramAggregationModel>();

  static {
    for (ExporterDefaultHistogramAggregationModel c : values()) {
      CONSTANTS.put(c.value, c);
    }
  }

  ExporterDefaultHistogramAggregationModel(String value) {
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
  public static ExporterDefaultHistogramAggregationModel fromValue(String value) {
    ExporterDefaultHistogramAggregationModel constant = CONSTANTS.get(value);
    if (constant == null) {
      throw new IllegalArgumentException(value);
    } else {
      return constant;
    }
  }
}
