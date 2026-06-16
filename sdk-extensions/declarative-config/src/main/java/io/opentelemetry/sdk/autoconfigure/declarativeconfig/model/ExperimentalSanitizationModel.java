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
@JsonPropertyOrder({"url"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalSanitizationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("url")
  private ExperimentalUrlSanitizationModel url;

  @JsonProperty("url")
  @Nullable
  public ExperimentalUrlSanitizationModel getUrl() {
    return url;
  }

  public ExperimentalSanitizationModel withUrl(ExperimentalUrlSanitizationModel url) {
    this.url = url;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalSanitizationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("url");
    sb.append('=');
    sb.append(((this.url == null) ? "<null>" : this.url));
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
    result = ((result * 31) + ((this.url == null) ? 0 : this.url.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalSanitizationModel) == false) {
      return false;
    }
    ExperimentalSanitizationModel rhs = ((ExperimentalSanitizationModel) other);
    return ((this.url == rhs.url) || ((this.url != null) && this.url.equals(rhs.url)));
  }
}
