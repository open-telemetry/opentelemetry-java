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
@JsonPropertyOrder({"attributes", "detectors"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalResourceDetectionModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("attributes")
  private IncludeExcludeModel attributes;

  /**
   * Configure resource detectors. Resource detector names are dependent on the SDK language
   * ecosystem. Please consult documentation for each respective language. If omitted, no resource
   * detectors are enabled.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("detectors")
  @JsonPropertyDescription(
      "Configure resource detectors.\nResource detector names are dependent on the SDK language ecosystem. Please consult documentation for each respective language. \nIf omitted, no resource detectors are enabled.\n")
  private List<ExperimentalResourceDetectorModel> detectors;

  @JsonProperty("attributes")
  @Nullable
  public IncludeExcludeModel getAttributes() {
    return attributes;
  }

  public ExperimentalResourceDetectionModel withAttributes(IncludeExcludeModel attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * Configure resource detectors. Resource detector names are dependent on the SDK language
   * ecosystem. Please consult documentation for each respective language. If omitted, no resource
   * detectors are enabled.
   */
  @JsonProperty("detectors")
  @Nullable
  public List<ExperimentalResourceDetectorModel> getDetectors() {
    return detectors;
  }

  public ExperimentalResourceDetectionModel withDetectors(
      List<ExperimentalResourceDetectorModel> detectors) {
    this.detectors = detectors;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalResourceDetectionModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("attributes");
    sb.append('=');
    sb.append(((this.attributes == null) ? "<null>" : this.attributes));
    sb.append(',');
    sb.append("detectors");
    sb.append('=');
    sb.append(((this.detectors == null) ? "<null>" : this.detectors));
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
    result = ((result * 31) + ((this.attributes == null) ? 0 : this.attributes.hashCode()));
    result = ((result * 31) + ((this.detectors == null) ? 0 : this.detectors.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalResourceDetectionModel) == false) {
      return false;
    }
    ExperimentalResourceDetectionModel rhs = ((ExperimentalResourceDetectionModel) other);
    return (((this.attributes == rhs.attributes)
            || ((this.attributes != null) && this.attributes.equals(rhs.attributes)))
        && ((this.detectors == rhs.detectors)
            || ((this.detectors != null) && this.detectors.equals(rhs.detectors))));
  }
}
