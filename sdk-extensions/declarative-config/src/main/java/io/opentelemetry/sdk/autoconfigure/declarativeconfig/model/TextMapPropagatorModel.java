/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TextMapPropagatorModel.BAGGAGE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TextMapPropagatorModel.B_3;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TextMapPropagatorModel.B_3_MULTI;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TextMapPropagatorModel.TRACECONTEXT;

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
@JsonPropertyOrder({TRACECONTEXT, BAGGAGE, B_3, B_3_MULTI})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class TextMapPropagatorModel {

  static final String TRACECONTEXT = "tracecontext";
  static final String BAGGAGE = "baggage";
  static final String B_3 = "b3";
  static final String B_3_MULTI = "b3multi";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(TRACECONTEXT, TraceContextPropagatorModel.class);
    STABLE_PROPERTIES.put(BAGGAGE, BaggagePropagatorModel.class);
    STABLE_PROPERTIES.put(B_3, B3PropagatorModel.class);
    STABLE_PROPERTIES.put(B_3_MULTI, B3MultiPropagatorModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private TraceContextPropagatorModel tracecontext;
  @Nullable private BaggagePropagatorModel baggage;
  @Nullable private B3PropagatorModel b3;
  @Nullable private B3MultiPropagatorModel b3multi;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Include the w3c trace context propagator.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(TRACECONTEXT)
  @Nullable
  public TraceContextPropagatorModel getTracecontext() {
    if (tracecontext == null) {
      return ExtensionPropertyUtil.getGraduated(
          TRACECONTEXT, extensionProperties, TraceContextPropagatorModel.class);
    }
    return tracecontext;
  }

  @JsonProperty(TRACECONTEXT)
  public TextMapPropagatorModel withTracecontext(TraceContextPropagatorModel tracecontext) {
    this.tracecontext = tracecontext;
    return this;
  }

  /**
   * Include the w3c baggage propagator.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(BAGGAGE)
  @Nullable
  public BaggagePropagatorModel getBaggage() {
    if (baggage == null) {
      return ExtensionPropertyUtil.getGraduated(
          BAGGAGE, extensionProperties, BaggagePropagatorModel.class);
    }
    return baggage;
  }

  @JsonProperty(BAGGAGE)
  public TextMapPropagatorModel withBaggage(BaggagePropagatorModel baggage) {
    this.baggage = baggage;
    return this;
  }

  /**
   * Include the zipkin b3 propagator.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(B_3)
  @Nullable
  public B3PropagatorModel getB3() {
    if (b3 == null) {
      return ExtensionPropertyUtil.getGraduated(B_3, extensionProperties, B3PropagatorModel.class);
    }
    return b3;
  }

  @JsonProperty(B_3)
  public TextMapPropagatorModel withB3(B3PropagatorModel b3) {
    this.b3 = b3;
    return this;
  }

  /**
   * Include the zipkin b3 multi propagator.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(B_3_MULTI)
  @Nullable
  public B3MultiPropagatorModel getB3multi() {
    if (b3multi == null) {
      return ExtensionPropertyUtil.getGraduated(
          B_3_MULTI, extensionProperties, B3MultiPropagatorModel.class);
    }
    return b3multi;
  }

  @JsonProperty(B_3_MULTI)
  public TextMapPropagatorModel withB3multi(B3MultiPropagatorModel b3multi) {
    this.b3multi = b3multi;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public TextMapPropagatorModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "TextMapPropagatorModel{"
        + "tracecontext="
        + tracecontext
        + ", baggage="
        + baggage
        + ", b3="
        + b3
        + ", b3multi="
        + b3multi
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.tracecontext == null) ? 0 : this.tracecontext.hashCode();
    h *= 1000003;
    h ^= (this.baggage == null) ? 0 : this.baggage.hashCode();
    h *= 1000003;
    h ^= (this.b3 == null) ? 0 : this.b3.hashCode();
    h *= 1000003;
    h ^= (this.b3multi == null) ? 0 : this.b3multi.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TextMapPropagatorModel) {
      TextMapPropagatorModel that = (TextMapPropagatorModel) o;
      return (this.tracecontext == null
              ? that.tracecontext == null
              : this.tracecontext.equals(that.tracecontext))
          && (this.baggage == null ? that.baggage == null : this.baggage.equals(that.baggage))
          && (this.b3 == null ? that.b3 == null : this.b3.equals(that.b3))
          && (this.b3multi == null ? that.b3multi == null : this.b3multi.equals(that.b3multi))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
