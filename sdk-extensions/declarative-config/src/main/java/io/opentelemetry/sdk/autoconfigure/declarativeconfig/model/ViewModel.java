/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel.SELECTOR;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ViewModel.STREAM;

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
@JsonPropertyOrder({SELECTOR, STREAM})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ViewModel {

  static final String SELECTOR = "selector";
  static final String STREAM = "stream";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(SELECTOR, ViewSelectorModel.class);
    STABLE_PROPERTIES.put(STREAM, ViewStreamModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private ViewSelectorModel selector;
  @Nullable private ViewStreamModel stream;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure view selector.
   *
   * <p>Selection criteria is additive as described in
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/sdk.md#instrument-selection-criteria.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(SELECTOR)
  @Nullable
  public ViewSelectorModel getSelector() {
    if (selector == null) {
      return ExtensionPropertyUtil.getGraduated(
          SELECTOR, extensionProperties, ViewSelectorModel.class);
    }
    return selector;
  }

  @JsonProperty(SELECTOR)
  public ViewModel withSelector(ViewSelectorModel selector) {
    this.selector = selector;
    return this;
  }

  /**
   * Configure view stream.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(STREAM)
  @Nullable
  public ViewStreamModel getStream() {
    if (stream == null) {
      return ExtensionPropertyUtil.getGraduated(STREAM, extensionProperties, ViewStreamModel.class);
    }
    return stream;
  }

  @JsonProperty(STREAM)
  public ViewModel withStream(ViewStreamModel stream) {
    this.stream = stream;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public ViewModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "ViewModel{"
        + "selector="
        + selector
        + ", stream="
        + stream
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.selector == null) ? 0 : this.selector.hashCode();
    h *= 1000003;
    h ^= (this.stream == null) ? 0 : this.stream.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ViewModel) {
      ViewModel that = (ViewModel) o;
      return (this.selector == null ? that.selector == null : this.selector.equals(that.selector))
          && (this.stream == null ? that.stream == null : this.stream.equals(that.stream))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
