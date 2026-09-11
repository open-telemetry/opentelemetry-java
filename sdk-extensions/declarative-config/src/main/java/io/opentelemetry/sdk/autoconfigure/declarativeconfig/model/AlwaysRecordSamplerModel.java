/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.AlwaysRecordSamplerModel.ROOT;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ROOT})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class AlwaysRecordSamplerModel {

  static final String ROOT = "root";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ROOT, SamplerModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private SamplerModel root;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure the wrapped sampler which provides the original sampling
   *
   * <p>decision that AlwaysRecord modifies. DROP decisions are converted
   *
   * <p>to RECORD_ONLY, allowing processors to see all spans without sending them to exporters.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(ROOT)
  @Nullable
  public SamplerModel getRoot() {
    if (root == null) {
      return ExtensionPropertyUtil.getGraduated(ROOT, extensionProperties, SamplerModel.class);
    }
    return root;
  }

  @JsonProperty(ROOT)
  public AlwaysRecordSamplerModel setRoot(SamplerModel root) {
    this.root = root;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public AlwaysRecordSamplerModel setExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "AlwaysRecordSamplerModel{"
        + "root="
        + root
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.getRoot() == null) ? 0 : this.getRoot().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof AlwaysRecordSamplerModel) {
      AlwaysRecordSamplerModel that = (AlwaysRecordSamplerModel) o;
      return (this.getRoot() == null
              ? that.getRoot() == null
              : this.getRoot().equals(that.getRoot()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
