/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"semconv"})
@Generated("jsonschema2pojo")
public class ExperimentalCodeInstrumentationModel {

  @JsonProperty("semconv")
  @Nullable
  private ExperimentalSemconvConfigModel semconv;

  @JsonProperty("semconv")
  @Nullable
  public ExperimentalSemconvConfigModel getSemconv() {
    return semconv;
  }

  public ExperimentalCodeInstrumentationModel withSemconv(ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalCodeInstrumentationModel{" + "semconv=" + semconv + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.semconv == null) ? 0 : this.semconv.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalCodeInstrumentationModel) {
      ExperimentalCodeInstrumentationModel that = (ExperimentalCodeInstrumentationModel) o;
      return (this.semconv == null ? that.semconv == null : this.semconv.equals(that.semconv));
    }
    return false;
  }
}
