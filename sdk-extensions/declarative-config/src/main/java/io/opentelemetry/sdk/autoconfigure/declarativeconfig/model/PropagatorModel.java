/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PropagatorModel.COMPOSITE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PropagatorModel.COMPOSITE_LIST;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({COMPOSITE, COMPOSITE_LIST})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PropagatorModel {

  static final String COMPOSITE = "composite";
  static final String COMPOSITE_LIST = "composite_list";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(COMPOSITE_LIST, String.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<TextMapPropagatorModel> composite;
  @Nullable private String compositeList;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure the propagators in the composite text map propagator. Entries from .composite_list
   * are appended to the list here with duplicates filtered out.
   *
   * <p>Built-in propagator keys include: tracecontext, baggage, b3, b3multi. Known third party keys
   * include: xray.
   *
   * <p>If omitted, and .composite_list is omitted or null, a noop propagator is used.
   */
  @JsonProperty(COMPOSITE)
  @Nullable
  public List<TextMapPropagatorModel> getComposite() {
    return composite;
  }

  @JsonProperty(COMPOSITE)
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
  @JsonProperty(COMPOSITE_LIST)
  @Nullable
  public String getCompositeList() {
    if (compositeList == null) {
      return ExtensionPropertyUtil.getGraduated(COMPOSITE_LIST, extensionProperties, String.class);
    }
    return compositeList;
  }

  @JsonProperty(COMPOSITE_LIST)
  public PropagatorModel withCompositeList(String compositeList) {
    this.compositeList = compositeList;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public PropagatorModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "PropagatorModel{"
        + "composite="
        + composite
        + ", compositeList="
        + compositeList
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.composite == null) ? 0 : this.composite.hashCode();
    h *= 1000003;
    h ^= (this.compositeList == null) ? 0 : this.compositeList.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
              : this.compositeList.equals(that.compositeList))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
