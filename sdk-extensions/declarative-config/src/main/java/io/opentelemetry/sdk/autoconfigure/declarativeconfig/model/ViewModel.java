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
@JsonPropertyOrder({"selector", "stream"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ViewModel {

  @Nullable private ViewSelectorModel selector;
  @Nullable private ViewStreamModel stream;

  /**
   * Configure view selector.
   *
   * <p>Selection criteria is additive as described in
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#instrument-selection-criteria.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("selector")
  @Nullable
  public ViewSelectorModel getSelector() {
    return selector;
  }

  @JsonProperty("selector")
  public ViewModel withSelector(ViewSelectorModel selector) {
    this.selector = selector;
    return this;
  }

  /**
   * Configure view stream.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("stream")
  @Nullable
  public ViewStreamModel getStream() {
    return stream;
  }

  @JsonProperty("stream")
  public ViewModel withStream(ViewStreamModel stream) {
    this.stream = stream;
    return this;
  }

  @Override
  public String toString() {
    return "ViewModel{" + "selector=" + selector + ", stream=" + stream + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.selector == null) ? 0 : this.selector.hashCode();
    h *= 1000003;
    h ^= (this.stream == null) ? 0 : this.stream.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ViewModel) {
      ViewModel that = (ViewModel) o;
      return (this.selector == null ? that.selector == null : this.selector.equals(that.selector))
          && (this.stream == null ? that.stream == null : this.stream.equals(that.stream));
    }
    return false;
  }
}
