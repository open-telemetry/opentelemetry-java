/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel.ATTRIBUTES;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel.ATTRIBUTES_LIST;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ResourceModel.SCHEMA_URL;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ResourceModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ATTRIBUTES, SCHEMA_URL, ATTRIBUTES_LIST})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ResourceModel {

  static final String ATTRIBUTES = "attributes";
  static final String SCHEMA_URL = "schema_url";
  static final String ATTRIBUTES_LIST = "attributes_list";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(SCHEMA_URL, String.class);
    STABLE_PROPERTIES.put(ATTRIBUTES_LIST, String.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<AttributeNameValueModel> attributes;
  @Nullable private String schemaUrl;
  @Nullable private String attributesList;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure resource attributes. Entries have higher priority than entries from
   * .resource.attributes_list.
   *
   * <p>If omitted, no resource attributes are added.
   */
  @JsonProperty(ATTRIBUTES)
  @Nullable
  public List<AttributeNameValueModel> getAttributes() {
    return attributes;
  }

  @JsonProperty(ATTRIBUTES)
  public ResourceModel withAttributes(List<AttributeNameValueModel> attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * Configure resource schema URL.
   *
   * <p>If omitted or null, no schema URL is used.
   */
  @JsonProperty(SCHEMA_URL)
  @Nullable
  public String getSchemaUrl() {
    if (schemaUrl == null) {
      return ExtensionPropertyUtil.getGraduated(SCHEMA_URL, extensionProperties, String.class);
    }
    return schemaUrl;
  }

  @JsonProperty(SCHEMA_URL)
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
  @JsonProperty(ATTRIBUTES_LIST)
  @Nullable
  public String getAttributesList() {
    if (attributesList == null) {
      return ExtensionPropertyUtil.getGraduated(ATTRIBUTES_LIST, extensionProperties, String.class);
    }
    return attributesList;
  }

  @JsonProperty(ATTRIBUTES_LIST)
  public ResourceModel withAttributesList(String attributesList) {
    this.attributesList = attributesList;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ResourceModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        EXPERIMENTAL_PROPERTIES,
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "ResourceModel{"
        + "attributes="
        + attributes
        + ", schemaUrl="
        + schemaUrl
        + ", attributesList="
        + attributesList
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.attributes == null) ? 0 : this.attributes.hashCode();
    h *= 1000003;
    h ^= (this.schemaUrl == null) ? 0 : this.schemaUrl.hashCode();
    h *= 1000003;
    h ^= (this.attributesList == null) ? 0 : this.attributesList.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
          && (this.schemaUrl == null
              ? that.schemaUrl == null
              : this.schemaUrl.equals(that.schemaUrl))
          && (this.attributesList == null
              ? that.attributesList == null
              : this.attributesList.equals(that.attributesList))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
