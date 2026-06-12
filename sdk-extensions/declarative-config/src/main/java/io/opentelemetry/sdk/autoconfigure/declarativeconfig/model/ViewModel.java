/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"selector", "stream"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ViewModel {

  /** (Required) */
  @JsonProperty("selector")
  @Nonnull
  private ViewSelectorModel selector;

  /** (Required) */
  @JsonProperty("stream")
  @Nonnull
  private ViewStreamModel stream;

  /** (Required) */
  @JsonProperty("selector")
  @Nullable
  public ViewSelectorModel getSelector() {
    return selector;
  }

  public ViewModel withSelector(ViewSelectorModel selector) {
    this.selector = selector;
    return this;
  }

  /** (Required) */
  @JsonProperty("stream")
  @Nullable
  public ViewStreamModel getStream() {
    return stream;
  }

  public ViewModel withStream(ViewStreamModel stream) {
    this.stream = stream;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ViewModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("selector");
    sb.append('=');
    sb.append(((this.selector == null) ? "<null>" : this.selector));
    sb.append(',');
    sb.append("stream");
    sb.append('=');
    sb.append(((this.stream == null) ? "<null>" : this.stream));
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
    result = ((result * 31) + ((this.stream == null) ? 0 : this.stream.hashCode()));
    result = ((result * 31) + ((this.selector == null) ? 0 : this.selector.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ViewModel) == false) {
      return false;
    }
    ViewModel rhs = ((ViewModel) other);
    return (((this.stream == rhs.stream)
            || ((this.stream != null) && this.stream.equals(rhs.stream)))
        && ((this.selector == rhs.selector)
            || ((this.selector != null) && this.selector.equals(rhs.selector))));
  }
}
