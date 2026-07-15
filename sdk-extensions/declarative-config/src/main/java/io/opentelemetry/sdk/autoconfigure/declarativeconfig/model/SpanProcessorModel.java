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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"batch", "simple"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class SpanProcessorModel {

  @Nullable private BatchSpanProcessorModel batch;
  @Nullable private SimpleSpanProcessorModel simple;
  private Map<String, SpanProcessorPropertyModel> additionalProperties =
      new LinkedHashMap<String, SpanProcessorPropertyModel>();

  /**
   * Configure a batch span processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("batch")
  @Nullable
  public BatchSpanProcessorModel getBatch() {
    return batch;
  }

  @JsonProperty("batch")
  public SpanProcessorModel withBatch(BatchSpanProcessorModel batch) {
    this.batch = batch;
    return this;
  }

  /**
   * Configure a simple span processor.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("simple")
  @Nullable
  public SimpleSpanProcessorModel getSimple() {
    return simple;
  }

  @JsonProperty("simple")
  public SpanProcessorModel withSimple(SimpleSpanProcessorModel simple) {
    this.simple = simple;
    return this;
  }

  @JsonAnyGetter
  public Map<String, SpanProcessorPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public SpanProcessorModel withAdditionalProperty(String name, SpanProcessorPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "SpanProcessorModel{"
        + "batch="
        + batch
        + ", simple="
        + simple
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
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
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
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
