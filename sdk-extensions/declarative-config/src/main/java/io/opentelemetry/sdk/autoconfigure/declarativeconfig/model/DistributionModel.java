/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class DistributionModel {

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return extensionProperties;
  }

  @JsonAnySetter
  public DistributionModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        Collections.emptyMap(),
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "DistributionModel{" + "extensionProperties=" + extensionProperties + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof DistributionModel) {
      DistributionModel that = (DistributionModel) o;
      return (this.extensionProperties == null
          ? that.extensionProperties == null
          : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
