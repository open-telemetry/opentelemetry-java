/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"composite", "composite_list"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class PropagatorModel {

  /**
   * Configure the propagators in the composite text map propagator. Entries from .composite_list
   * are appended to the list here with duplicates filtered out. Built-in propagator keys include:
   * tracecontext, baggage, b3, b3multi. Known third party keys include: xray. If omitted, and
   * .composite_list is omitted or null, a noop propagator is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("composite")
  @JsonPropertyDescription(
      "Configure the propagators in the composite text map propagator. Entries from .composite_list are appended to the list here with duplicates filtered out.\nBuilt-in propagator keys include: tracecontext, baggage, b3, b3multi. Known third party keys include: xray.\nIf omitted, and .composite_list is omitted or null, a noop propagator is used.\n")
  private List<TextMapPropagatorModel> composite;

  /**
   * Configure the propagators in the composite text map propagator. Entries are appended to
   * .composite with duplicates filtered out. The value is a comma separated list of propagator
   * identifiers matching the format of OTEL_PROPAGATORS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details. Built-in propagator identifiers include: tracecontext, baggage, b3, b3multi. Known
   * third party identifiers include: xray. If omitted or null, and .composite is omitted or null, a
   * noop propagator is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("composite_list")
  @JsonPropertyDescription(
      "Configure the propagators in the composite text map propagator. Entries are appended to .composite with duplicates filtered out.\nThe value is a comma separated list of propagator identifiers matching the format of OTEL_PROPAGATORS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration for details.\nBuilt-in propagator identifiers include: tracecontext, baggage, b3, b3multi. Known third party identifiers include: xray.\nIf omitted or null, and .composite is omitted or null, a noop propagator is used.\n")
  private String compositeList;

  /**
   * Configure the propagators in the composite text map propagator. Entries from .composite_list
   * are appended to the list here with duplicates filtered out. Built-in propagator keys include:
   * tracecontext, baggage, b3, b3multi. Known third party keys include: xray. If omitted, and
   * .composite_list is omitted or null, a noop propagator is used.
   */
  @JsonProperty("composite")
  @Nullable
  public List<TextMapPropagatorModel> getComposite() {
    return composite;
  }

  public PropagatorModel withComposite(List<TextMapPropagatorModel> composite) {
    this.composite = composite;
    return this;
  }

  /**
   * Configure the propagators in the composite text map propagator. Entries are appended to
   * .composite with duplicates filtered out. The value is a comma separated list of propagator
   * identifiers matching the format of OTEL_PROPAGATORS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details. Built-in propagator identifiers include: tracecontext, baggage, b3, b3multi. Known
   * third party identifiers include: xray. If omitted or null, and .composite is omitted or null, a
   * noop propagator is used.
   */
  @JsonProperty("composite_list")
  @Nullable
  public String getCompositeList() {
    return compositeList;
  }

  public PropagatorModel withCompositeList(String compositeList) {
    this.compositeList = compositeList;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(PropagatorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("composite");
    sb.append('=');
    sb.append(((this.composite == null) ? "<null>" : this.composite));
    sb.append(',');
    sb.append("compositeList");
    sb.append('=');
    sb.append(((this.compositeList == null) ? "<null>" : this.compositeList));
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
    result = ((result * 31) + ((this.composite == null) ? 0 : this.composite.hashCode()));
    result = ((result * 31) + ((this.compositeList == null) ? 0 : this.compositeList.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof PropagatorModel) == false) {
      return false;
    }
    PropagatorModel rhs = ((PropagatorModel) other);
    return (((this.composite == rhs.composite)
            || ((this.composite != null) && this.composite.equals(rhs.composite)))
        && ((this.compositeList == rhs.compositeList)
            || ((this.compositeList != null) && this.compositeList.equals(rhs.compositeList))));
  }
}
