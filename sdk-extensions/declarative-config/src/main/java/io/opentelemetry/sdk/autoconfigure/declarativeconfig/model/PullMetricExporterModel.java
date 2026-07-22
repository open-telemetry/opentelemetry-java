/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.PullMetricExporterModelAccessor.EXPERIMENTAL_PROPERTIES;

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
public class PullMetricExporterModel {

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return extensionProperties;
  }

  @JsonAnySetter
  public PullMetricExporterModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        EXPERIMENTAL_PROPERTIES,
        Collections.emptyMap(),
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "PullMetricExporterModel{" + "extensionProperties=" + extensionProperties + "}";
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
    if (o instanceof PullMetricExporterModel) {
      PullMetricExporterModel that = (PullMetricExporterModel) o;
      return (this.extensionProperties == null
          ? that.extensionProperties == null
          : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
