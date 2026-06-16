/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"batch", "simple"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class LogRecordProcessorModel {

  @Nullable
  @JsonProperty("batch")
  private BatchLogRecordProcessorModel batch;

  @Nullable
  @JsonProperty("simple")
  private SimpleLogRecordProcessorModel simple;

  @JsonIgnore
  private Map<String, LogRecordProcessorPropertyModel> additionalProperties =
      new LinkedHashMap<String, LogRecordProcessorPropertyModel>();

  @JsonProperty("batch")
  @Nullable
  public BatchLogRecordProcessorModel getBatch() {
    return batch;
  }

  public LogRecordProcessorModel withBatch(BatchLogRecordProcessorModel batch) {
    this.batch = batch;
    return this;
  }

  @JsonProperty("simple")
  @Nullable
  public SimpleLogRecordProcessorModel getSimple() {
    return simple;
  }

  public LogRecordProcessorModel withSimple(SimpleLogRecordProcessorModel simple) {
    this.simple = simple;
    return this;
  }

  @JsonAnyGetter
  public Map<String, LogRecordProcessorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, LogRecordProcessorPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public LogRecordProcessorModel withAdditionalProperty(
      String name, LogRecordProcessorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(LogRecordProcessorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("batch");
    sb.append('=');
    sb.append(((this.batch == null) ? "<null>" : this.batch));
    sb.append(',');
    sb.append("simple");
    sb.append('=');
    sb.append(((this.simple == null) ? "<null>" : this.simple));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
    sb.append(',');
    if (sb.charAt((sb.length() - 1)) == ',') {
      sb.setCharAt((sb.length() - 1), ']');
    } else {
      sb.append(']');
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = ((result * 31) + ((this.batch == null) ? 0 : this.batch.hashCode()));
    result = ((result * 31) + ((this.simple == null) ? 0 : this.simple.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof LogRecordProcessorModel) == false) {
      return false;
    }
    LogRecordProcessorModel rhs = ((LogRecordProcessorModel) other);
    return ((((this.batch == rhs.batch) || ((this.batch != null) && this.batch.equals(rhs.batch)))
            && ((this.simple == rhs.simple)
                || ((this.simple != null) && this.simple.equals(rhs.simple))))
        && ((this.additionalProperties == rhs.additionalProperties)
            || ((this.additionalProperties != null)
                && this.additionalProperties.equals(rhs.additionalProperties))));
  }
}
