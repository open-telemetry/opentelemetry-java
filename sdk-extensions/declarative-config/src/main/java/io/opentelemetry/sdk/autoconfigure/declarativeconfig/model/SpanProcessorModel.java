/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel.BATCH;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanProcessorModel.SIMPLE;

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
@JsonPropertyOrder({BATCH, SIMPLE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class SpanProcessorModel {

  static final String BATCH = "batch";
  static final String SIMPLE = "simple";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(BATCH, BatchSpanProcessorModel.class);
    STABLE_PROPERTIES.put(SIMPLE, SimpleSpanProcessorModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private BatchSpanProcessorModel batch;
  @Nullable private SimpleSpanProcessorModel simple;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure a batch span processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(BATCH)
  @Nullable
  public BatchSpanProcessorModel getBatch() {
    if (batch == null) {
      return ExtensionPropertyUtil.getGraduated(
          BATCH, extensionProperties, BatchSpanProcessorModel.class);
    }
    return batch;
  }

  @JsonProperty(BATCH)
  public SpanProcessorModel withBatch(BatchSpanProcessorModel batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Configure a simple span processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(SIMPLE)
  @Nullable
  public SimpleSpanProcessorModel getSimple() {
    if (simple == null) {
      return ExtensionPropertyUtil.getGraduated(
          SIMPLE, extensionProperties, SimpleSpanProcessorModel.class);
    }
    return simple;
  }

  @JsonProperty(SIMPLE)
  public SpanProcessorModel withSimple(SimpleSpanProcessorModel simple) {
    this.simple = simple;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public SpanProcessorModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "SpanProcessorModel{"
        + "batch="
        + batch
        + ", simple="
        + simple
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.batch == null) ? 0 : this.batch.hashCode();
    h *= 1000003;
    h ^= (this.simple == null) ? 0 : this.simple.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanProcessorModel) {
      SpanProcessorModel that = (SpanProcessorModel) o;
      return (this.batch == null ? that.batch == null : this.batch.equals(that.batch))
          && (this.simple == null ? that.simple == null : this.simple.equals(that.simple))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
