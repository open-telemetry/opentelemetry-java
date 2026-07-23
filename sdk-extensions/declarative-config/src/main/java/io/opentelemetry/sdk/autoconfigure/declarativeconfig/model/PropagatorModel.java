/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"composite", "composite_list"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PropagatorModel {

  @Nullable private List<TextMapPropagatorModel> composite;
  @Nullable private String compositeList;

  /**
   * Configure the propagators in the composite text map propagator. Entries from .composite_list
   * are appended to the list here with duplicates filtered out.
   *
   * <p>Built-in propagator keys include: tracecontext, baggage, b3, b3multi. Known third party keys
   * include: xray.
   *
   * <p>If omitted, and .composite_list is omitted or null, a noop propagator is used.
   */
  @JsonProperty("composite")
  @Nullable
  public List<TextMapPropagatorModel> getComposite() {
    return composite;
  }

  @JsonProperty("composite")
  public PropagatorModel withComposite(List<TextMapPropagatorModel> composite) {
    this.composite = composite;
    return this;
  }

  /**
   * Configure the propagators in the composite text map propagator. Entries are appended to
   * .composite with duplicates filtered out.
   *
   * <p>The value is a comma separated list of propagator identifiers matching the format of
   * OTEL_PROPAGATORS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details.
   *
   * <p>Built-in propagator identifiers include: tracecontext, baggage, b3, b3multi. Known third
   * party identifiers include: xray.
   *
   * <p>If omitted or null, and .composite is omitted or null, a noop propagator is used.
   */
  @JsonProperty("composite_list")
  @Nullable
  public String getCompositeList() {
    return compositeList;
  }

  @JsonProperty("composite_list")
  public PropagatorModel withCompositeList(String compositeList) {
    this.compositeList = compositeList;
    return this;
  }

  @Override
  public String toString() {
    return "PropagatorModel{" + "composite=" + composite + ", compositeList=" + compositeList + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.composite == null) ? 0 : this.composite.hashCode();
    h *= 1000003;
    h ^= (this.compositeList == null) ? 0 : this.compositeList.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PropagatorModel) {
      PropagatorModel that = (PropagatorModel) o;
      return (this.composite == null
              ? that.composite == null
              : this.composite.equals(that.composite))
          && (this.compositeList == null
              ? that.compositeList == null
              : this.compositeList.equals(that.compositeList));
    }
    return false;
  }
}
