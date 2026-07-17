/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

/**
 * **Deprecated** as of v1.2.0, may be removed in v2.0.0. The OpenCensus compatibility specification
 * it relies on was deprecated in
 * https://github.com/open-telemetry/opentelemetry-specification/pull/5138. See also the [OpenCensus
 * sunset announcement](https://opentelemetry.io/blog/2023/sunsetting-opencensus/).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({})
@Generated("jsonschema2pojo")
public class OpenCensusMetricProducerModel {

  @Override
  public String toString() {
    return "OpenCensusMetricProducerModel{" + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OpenCensusMetricProducerModel) {
      OpenCensusMetricProducerModel that = (OpenCensusMetricProducerModel) o;
      return true;
    }
    return false;
  }
}
