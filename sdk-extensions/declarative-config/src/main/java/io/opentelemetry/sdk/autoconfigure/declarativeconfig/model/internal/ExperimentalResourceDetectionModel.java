/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.IncludeExcludeModel;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"attributes", "detectors"})
@Generated("jsonschema2pojo")
public class ExperimentalResourceDetectionModel {

  @JsonProperty("attributes")
  @Nullable
  private IncludeExcludeModel attributes;

  /**
   * Configure resource detectors. Resource detector names are dependent on the SDK language
   * ecosystem. Please consult documentation for each respective language. If omitted, no resource
   * detectors are enabled.
   */
  @JsonProperty("detectors")
  @JsonPropertyDescription(
      "Configure resource detectors.\nResource detector names are dependent on the SDK language ecosystem. Please consult documentation for each respective language. \nIf omitted, no resource detectors are enabled.\n")
  @Nullable
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
    return "ExperimentalResourceDetectionModel{"
        + "attributes="
        + attributes
        + ", detectors="
        + detectors
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributes == null) ? 0 : this.attributes.hashCode();
    h *= 1000003;
    h ^= (this.detectors == null) ? 0 : this.detectors.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalResourceDetectionModel) {
      ExperimentalResourceDetectionModel that = (ExperimentalResourceDetectionModel) o;
      return (this.attributes == null
              ? that.attributes == null
              : this.attributes.equals(that.attributes))
          && (this.detectors == null
              ? that.detectors == null
              : this.detectors.equals(that.detectors));
    }
    return false;
  }
}
