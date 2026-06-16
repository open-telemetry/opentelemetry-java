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
@JsonPropertyOrder({"attributes", "detection/development", "schema_url", "attributes_list"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ResourceModel {

  /**
   * Configure resource attributes. Entries have higher priority than entries from
   * .resource.attributes_list. If omitted, no resource attributes are added.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attributes")
  @JsonPropertyDescription(
      "Configure resource attributes. Entries have higher priority than entries from .resource.attributes_list.\nIf omitted, no resource attributes are added.\n")
  private List<AttributeNameValueModel> attributes;

  /** (Can be null) */
  @Nullable
  @JsonProperty("detection/development")
  private ExperimentalResourceDetectionModel detectionDevelopment;

  /**
   * Configure resource schema URL. If omitted or null, no schema URL is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("schema_url")
  @JsonPropertyDescription(
      "Configure resource schema URL.\nIf omitted or null, no schema URL is used.\n")
  private String schemaUrl;

  /**
   * Configure resource attributes. Entries have lower priority than entries from
   * .resource.attributes. The value is a list of comma separated key-value pairs matching the
   * format of OTEL_RESOURCE_ATTRIBUTES. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details. If omitted or null, no resource attributes are added.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("attributes_list")
  @JsonPropertyDescription(
      "Configure resource attributes. Entries have lower priority than entries from .resource.attributes.\nThe value is a list of comma separated key-value pairs matching the format of OTEL_RESOURCE_ATTRIBUTES. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration for details.\nIf omitted or null, no resource attributes are added.\n")
  private String attributesList;

  /**
   * Configure resource attributes. Entries have higher priority than entries from
   * .resource.attributes_list. If omitted, no resource attributes are added.
   */
  @JsonProperty("attributes")
  @Nullable
  public List<AttributeNameValueModel> getAttributes() {
    return attributes;
  }

  public ResourceModel withAttributes(List<AttributeNameValueModel> attributes) {
    this.attributes = attributes;
    return this;
  }

  @JsonProperty("detection/development")
  @Nullable
  public ExperimentalResourceDetectionModel getDetectionDevelopment() {
    return detectionDevelopment;
  }

  public ResourceModel withDetectionDevelopment(
      ExperimentalResourceDetectionModel detectionDevelopment) {
    this.detectionDevelopment = detectionDevelopment;
    return this;
  }

  /** Configure resource schema URL. If omitted or null, no schema URL is used. */
  @JsonProperty("schema_url")
  @Nullable
  public String getSchemaUrl() {
    return schemaUrl;
  }

  public ResourceModel withSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /**
   * Configure resource attributes. Entries have lower priority than entries from
   * .resource.attributes. The value is a list of comma separated key-value pairs matching the
   * format of OTEL_RESOURCE_ATTRIBUTES. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details. If omitted or null, no resource attributes are added.
   */
  @JsonProperty("attributes_list")
  @Nullable
  public String getAttributesList() {
    return attributesList;
  }

  public ResourceModel withAttributesList(String attributesList) {
    this.attributesList = attributesList;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ResourceModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("attributes");
    sb.append('=');
    sb.append(((this.attributes == null) ? "<null>" : this.attributes));
    sb.append(',');
    sb.append("detectionDevelopment");
    sb.append('=');
    sb.append(((this.detectionDevelopment == null) ? "<null>" : this.detectionDevelopment));
    sb.append(',');
    sb.append("schemaUrl");
    sb.append('=');
    sb.append(((this.schemaUrl == null) ? "<null>" : this.schemaUrl));
    sb.append(',');
    sb.append("attributesList");
    sb.append('=');
    sb.append(((this.attributesList == null) ? "<null>" : this.attributesList));
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
    result =
        ((result * 31)
            + ((this.detectionDevelopment == null) ? 0 : this.detectionDevelopment.hashCode()));
    result = ((result * 31) + ((this.attributesList == null) ? 0 : this.attributesList.hashCode()));
    result = ((result * 31) + ((this.schemaUrl == null) ? 0 : this.schemaUrl.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ResourceModel) == false) {
      return false;
    }
    ResourceModel rhs = ((ResourceModel) other);
    return (((((this.attributes == rhs.attributes)
                    || ((this.attributes != null) && this.attributes.equals(rhs.attributes)))
                && ((this.detectionDevelopment == rhs.detectionDevelopment)
                    || ((this.detectionDevelopment != null)
                        && this.detectionDevelopment.equals(rhs.detectionDevelopment))))
            && ((this.attributesList == rhs.attributesList)
                || ((this.attributesList != null)
                    && this.attributesList.equals(rhs.attributesList))))
        && ((this.schemaUrl == rhs.schemaUrl)
            || ((this.schemaUrl != null) && this.schemaUrl.equals(rhs.schemaUrl))));
  }
}
