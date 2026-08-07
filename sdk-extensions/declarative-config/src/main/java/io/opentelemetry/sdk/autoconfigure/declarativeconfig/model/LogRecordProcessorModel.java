/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel.BATCH;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordProcessorModel.SIMPLE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.LogRecordProcessorModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({BATCH, SIMPLE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class LogRecordProcessorModel {

  static final String BATCH = "batch";
  static final String SIMPLE = "simple";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(BATCH, BatchLogRecordProcessorModel.class);
    STABLE_PROPERTIES.put(SIMPLE, SimpleLogRecordProcessorModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private BatchLogRecordProcessorModel batch;
  @Nullable private SimpleLogRecordProcessorModel simple;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure a batch log record processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(BATCH)
  @Nullable
  public BatchLogRecordProcessorModel getBatch() {
    if (batch == null) {
      return ExtensionPropertyUtil.getGraduated(
          BATCH, extensionProperties, BatchLogRecordProcessorModel.class);
    }
    return batch;
  }

  @JsonProperty(BATCH)
  public LogRecordProcessorModel withBatch(BatchLogRecordProcessorModel batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Configure a simple log record processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(SIMPLE)
  @Nullable
  public SimpleLogRecordProcessorModel getSimple() {
    if (simple == null) {
      return ExtensionPropertyUtil.getGraduated(
          SIMPLE, extensionProperties, SimpleLogRecordProcessorModel.class);
    }
    return simple;
  }

  @JsonProperty(SIMPLE)
  public LogRecordProcessorModel withSimple(SimpleLogRecordProcessorModel simple) {
    this.simple = simple;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public LogRecordProcessorModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "LogRecordProcessorModel{"
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
    if (o instanceof LogRecordProcessorModel) {
      LogRecordProcessorModel that = (LogRecordProcessorModel) o;
      return (this.batch == null ? that.batch == null : this.batch.equals(that.batch))
          && (this.simple == null ? that.simple == null : this.simple.equals(that.simple))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
