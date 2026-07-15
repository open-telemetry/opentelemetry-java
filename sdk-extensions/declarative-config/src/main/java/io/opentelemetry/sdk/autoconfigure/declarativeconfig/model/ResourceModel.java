/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalResourceDetectionModel;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"attributes", "detection/development", "schema_url", "attributes_list"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ResourceModel {

  @Nullable private List<AttributeNameValueModel> attributes;
  @Nullable private ExperimentalResourceDetectionModel detectionDevelopment;
  @Nullable private String schemaUrl;
  @Nullable private String attributesList;

  /**
   * Configure resource attributes. Entries have higher priority than entries from
   * .resource.attributes_list.
   *
   * <p>If omitted, no resource attributes are added.
   */
  @JsonProperty("attributes")
  @Nullable
  public List<AttributeNameValueModel> getAttributes() {
    return attributes;
  }

  @JsonProperty("attributes")
  public ResourceModel withAttributes(List<AttributeNameValueModel> attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * Configure resource detection.
   *
   * <p>If omitted, resource detection is disabled.
   */
  @JsonProperty("detection/development")
  @Nullable
  public ExperimentalResourceDetectionModel getDetectionDevelopment() {
    return detectionDevelopment;
  }

  @JsonProperty("detection/development")
  public ResourceModel withDetectionDevelopment(
      ExperimentalResourceDetectionModel detectionDevelopment) {
    this.detectionDevelopment = detectionDevelopment;
    return this;
  }

  /**
   * Configure resource schema URL.
   *
   * <p>If omitted or null, no schema URL is used.
   */
  @JsonProperty("schema_url")
  @Nullable
  public String getSchemaUrl() {
    return schemaUrl;
  }

  @JsonProperty("schema_url")
  public ResourceModel withSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /**
   * Configure resource attributes. Entries have lower priority than entries from
   * .resource.attributes.
   *
   * <p>The value is a list of comma separated key-value pairs matching the format of
   * OTEL_RESOURCE_ATTRIBUTES. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#general-sdk-configuration
   * for details.
   *
   * <p>If omitted or null, no resource attributes are added.
   */
  @JsonProperty("attributes_list")
  @Nullable
  public String getAttributesList() {
    return attributesList;
  }

  @JsonProperty("attributes_list")
  public ResourceModel withAttributesList(String attributesList) {
    this.attributesList = attributesList;
    return this;
  }

  @Override
  public String toString() {
    return "ResourceModel{"
        + "attributes="
        + attributes
        + ", detectionDevelopment="
        + detectionDevelopment
        + ", schemaUrl="
        + schemaUrl
        + ", attributesList="
        + attributesList
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributes == null) ? 0 : this.attributes.hashCode();
    h *= 1000003;
    h ^= (this.detectionDevelopment == null) ? 0 : this.detectionDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.schemaUrl == null) ? 0 : this.schemaUrl.hashCode();
    h *= 1000003;
    h ^= (this.attributesList == null) ? 0 : this.attributesList.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ResourceModel) {
      ResourceModel that = (ResourceModel) o;
      return (this.attributes == null
              ? that.attributes == null
              : this.attributes.equals(that.attributes))
          && (this.detectionDevelopment == null
              ? that.detectionDevelopment == null
              : this.detectionDevelopment.equals(that.detectionDevelopment))
          && (this.schemaUrl == null
              ? that.schemaUrl == null
              : this.schemaUrl.equals(that.schemaUrl))
          && (this.attributesList == null
              ? that.attributesList == null
              : this.attributesList.equals(that.attributesList));
    }
    return false;
  }
}
