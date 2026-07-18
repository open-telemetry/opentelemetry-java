/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalEventToSpanEventBridgeLogRecordProcessorModel;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"batch", "simple", "event_to_span_event_bridge/development"})
@Generated("jsonschema2pojo")
public class LogRecordProcessorModel {

  @Nullable private BatchLogRecordProcessorModel batch;
  @Nullable private SimpleLogRecordProcessorModel simple;

  @Nullable
  private ExperimentalEventToSpanEventBridgeLogRecordProcessorModel
      eventToSpanEventBridgeDevelopment;

  private Map<String, LogRecordProcessorPropertyModel> additionalProperties =
      new LinkedHashMap<String, LogRecordProcessorPropertyModel>();

  /**
   * Configure a batch log record processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("batch")
  @Nullable
  public BatchLogRecordProcessorModel getBatch() {
    return batch;
  }

  @JsonProperty("batch")
  public LogRecordProcessorModel withBatch(BatchLogRecordProcessorModel batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Configure a simple log record processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("simple")
  @Nullable
  public SimpleLogRecordProcessorModel getSimple() {
    return simple;
  }

  @JsonProperty("simple")
  public LogRecordProcessorModel withSimple(SimpleLogRecordProcessorModel simple) {
    this.simple = simple;
    return this;
  }

  /**
   * Configure an event to span event bridge log record processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("event_to_span_event_bridge/development")
  @Nullable
  public ExperimentalEventToSpanEventBridgeLogRecordProcessorModel
      getEventToSpanEventBridgeDevelopment() {
    return eventToSpanEventBridgeDevelopment;
  }

  @JsonProperty("event_to_span_event_bridge/development")
  public LogRecordProcessorModel withEventToSpanEventBridgeDevelopment(
      ExperimentalEventToSpanEventBridgeLogRecordProcessorModel eventToSpanEventBridgeDevelopment) {
    this.eventToSpanEventBridgeDevelopment = eventToSpanEventBridgeDevelopment;
    return this;
  }

  @JsonAnyGetter
  public Map<String, LogRecordProcessorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public LogRecordProcessorModel withAdditionalProperty(
      String name, LogRecordProcessorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "LogRecordProcessorModel{"
        + "batch="
        + batch
        + ", simple="
        + simple
        + ", eventToSpanEventBridgeDevelopment="
        + eventToSpanEventBridgeDevelopment
        + ", additionalProperties="
        + additionalProperties
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
    h ^=
        (this.eventToSpanEventBridgeDevelopment == null)
            ? 0
            : this.eventToSpanEventBridgeDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
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
          && (this.eventToSpanEventBridgeDevelopment == null
              ? that.eventToSpanEventBridgeDevelopment == null
              : this.eventToSpanEventBridgeDevelopment.equals(
                  that.eventToSpanEventBridgeDevelopment))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
