/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"semconv"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalMessagingInstrumentationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("semconv")
  private ExperimentalSemconvConfigModel semconv;

  @JsonProperty("semconv")
  @Nullable
  public ExperimentalSemconvConfigModel getSemconv() {
    return semconv;
  }

  public ExperimentalMessagingInstrumentationModel withSemconv(
      ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalMessagingInstrumentationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("semconv");
    sb.append('=');
    sb.append(((this.semconv == null) ? "<null>" : this.semconv));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.semconv == null) ? 0 : this.semconv.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalMessagingInstrumentationModel) == false) {
      return false;
    }
    ExperimentalMessagingInstrumentationModel rhs =
        ((ExperimentalMessagingInstrumentationModel) other);
    return ((this.semconv == rhs.semconv)
        || ((this.semconv != null) && this.semconv.equals(rhs.semconv)));
  }
}
