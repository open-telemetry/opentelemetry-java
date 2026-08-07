/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel.EXPORTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel.EXPORT_TIMEOUT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel.MAX_EXPORT_BATCH_SIZE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel.MAX_QUEUE_SIZE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.BatchSpanProcessorModel.SCHEDULE_DELAY;

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
@JsonPropertyOrder({
  SCHEDULE_DELAY,
  EXPORT_TIMEOUT,
  MAX_QUEUE_SIZE,
  MAX_EXPORT_BATCH_SIZE,
  EXPORTER
})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class BatchSpanProcessorModel {

  static final String SCHEDULE_DELAY = "schedule_delay";
  static final String EXPORT_TIMEOUT = "export_timeout";
  static final String MAX_QUEUE_SIZE = "max_queue_size";
  static final String MAX_EXPORT_BATCH_SIZE = "max_export_batch_size";
  static final String EXPORTER = "exporter";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(SCHEDULE_DELAY, Integer.class);
    STABLE_PROPERTIES.put(EXPORT_TIMEOUT, Integer.class);
    STABLE_PROPERTIES.put(MAX_QUEUE_SIZE, Integer.class);
    STABLE_PROPERTIES.put(MAX_EXPORT_BATCH_SIZE, Integer.class);
    STABLE_PROPERTIES.put(EXPORTER, SpanExporterModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private Integer scheduleDelay;
  @Nullable private Integer exportTimeout;
  @Nullable private Integer maxQueueSize;
  @Nullable private Integer maxExportBatchSize;
  @Nullable private SpanExporterModel exporter;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure delay interval (in milliseconds) between two consecutive exports.
   *
   * <p>Value must be non-negative.
   *
   * <p>If omitted or null, 5000 is used.
   */
  @JsonProperty(SCHEDULE_DELAY)
  @Nullable
  public Integer getScheduleDelay() {
    if (scheduleDelay == null) {
      return ExtensionPropertyUtil.getGraduated(SCHEDULE_DELAY, extensionProperties, Integer.class);
    }
    return scheduleDelay;
  }

  @JsonProperty(SCHEDULE_DELAY)
  public BatchSpanProcessorModel withScheduleDelay(Integer scheduleDelay) {
    this.scheduleDelay = scheduleDelay;
    return this;
  }

  /**
   * Configure maximum allowed time (in milliseconds) to export data.
   *
   * <p>Value must be non-negative. A value of 0 indicates no limit (infinity).
   *
   * <p>If omitted or null, 30000 is used.
   */
  @JsonProperty(EXPORT_TIMEOUT)
  @Nullable
  public Integer getExportTimeout() {
    if (exportTimeout == null) {
      return ExtensionPropertyUtil.getGraduated(EXPORT_TIMEOUT, extensionProperties, Integer.class);
    }
    return exportTimeout;
  }

  @JsonProperty(EXPORT_TIMEOUT)
  public BatchSpanProcessorModel withExportTimeout(Integer exportTimeout) {
    this.exportTimeout = exportTimeout;
    return this;
  }

  /**
   * Configure maximum queue size. Value must be positive.
   *
   * <p>If omitted or null, 2048 is used.
   */
  @JsonProperty(MAX_QUEUE_SIZE)
  @Nullable
  public Integer getMaxQueueSize() {
    if (maxQueueSize == null) {
      return ExtensionPropertyUtil.getGraduated(MAX_QUEUE_SIZE, extensionProperties, Integer.class);
    }
    return maxQueueSize;
  }

  @JsonProperty(MAX_QUEUE_SIZE)
  public BatchSpanProcessorModel withMaxQueueSize(Integer maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  /**
   * Configure maximum batch size. Value must be positive.
   *
   * <p>If omitted or null, 512 is used.
   */
  @JsonProperty(MAX_EXPORT_BATCH_SIZE)
  @Nullable
  public Integer getMaxExportBatchSize() {
    if (maxExportBatchSize == null) {
      return ExtensionPropertyUtil.getGraduated(
          MAX_EXPORT_BATCH_SIZE, extensionProperties, Integer.class);
    }
    return maxExportBatchSize;
  }

  @JsonProperty(MAX_EXPORT_BATCH_SIZE)
  public BatchSpanProcessorModel withMaxExportBatchSize(Integer maxExportBatchSize) {
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(EXPORTER)
  @Nullable
  public SpanExporterModel getExporter() {
    if (exporter == null) {
      return ExtensionPropertyUtil.getGraduated(
          EXPORTER, extensionProperties, SpanExporterModel.class);
    }
    return exporter;
  }

  @JsonProperty(EXPORTER)
  public BatchSpanProcessorModel withExporter(SpanExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public BatchSpanProcessorModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "BatchSpanProcessorModel{"
        + "scheduleDelay="
        + scheduleDelay
        + ", exportTimeout="
        + exportTimeout
        + ", maxQueueSize="
        + maxQueueSize
        + ", maxExportBatchSize="
        + maxExportBatchSize
        + ", exporter="
        + exporter
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.scheduleDelay == null) ? 0 : this.scheduleDelay.hashCode();
    h *= 1000003;
    h ^= (this.exportTimeout == null) ? 0 : this.exportTimeout.hashCode();
    h *= 1000003;
    h ^= (this.maxQueueSize == null) ? 0 : this.maxQueueSize.hashCode();
    h *= 1000003;
    h ^= (this.maxExportBatchSize == null) ? 0 : this.maxExportBatchSize.hashCode();
    h *= 1000003;
    h ^= (this.exporter == null) ? 0 : this.exporter.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof BatchSpanProcessorModel) {
      BatchSpanProcessorModel that = (BatchSpanProcessorModel) o;
      return (this.scheduleDelay == null
              ? that.scheduleDelay == null
              : this.scheduleDelay.equals(that.scheduleDelay))
          && (this.exportTimeout == null
              ? that.exportTimeout == null
              : this.exportTimeout.equals(that.exportTimeout))
          && (this.maxQueueSize == null
              ? that.maxQueueSize == null
              : this.maxQueueSize.equals(that.maxQueueSize))
          && (this.maxExportBatchSize == null
              ? that.maxExportBatchSize == null
              : this.maxExportBatchSize.equals(that.maxExportBatchSize))
          && (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
