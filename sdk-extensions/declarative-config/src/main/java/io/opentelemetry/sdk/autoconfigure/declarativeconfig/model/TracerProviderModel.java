/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalTracerConfiguratorModel;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "processors",
  "limits",
  "sampler",
  "id_generator",
  "tracer_configurator/development"
})
@Generated("jsonschema2pojo")
public class TracerProviderModel {

  /**
   * Configure span processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @JsonPropertyDescription(
      "Configure span processors.\nProperty is required and must be non-null.\n")
  @Nullable
  private List<SpanProcessorModel> processors;

  @JsonProperty("limits")
  @Nullable
  private SpanLimitsModel limits;

  @JsonProperty("sampler")
  @Nullable
  private SamplerModel sampler;

  @JsonProperty("id_generator")
  @Nullable
  private IdGeneratorModel idGenerator;

  @JsonProperty("tracer_configurator/development")
  @Nullable
  private ExperimentalTracerConfiguratorModel tracerConfiguratorDevelopment;

  /**
   * Configure span processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @Nullable
  public List<SpanProcessorModel> getProcessors() {
    return processors;
  }

  public TracerProviderModel withProcessors(List<SpanProcessorModel> processors) {
    this.processors = processors;
    return this;
  }

  @JsonProperty("limits")
  @Nullable
  public SpanLimitsModel getLimits() {
    return limits;
  }

  public TracerProviderModel withLimits(SpanLimitsModel limits) {
    this.limits = limits;
    return this;
  }

  @JsonProperty("sampler")
  @Nullable
  public SamplerModel getSampler() {
    return sampler;
  }

  public TracerProviderModel withSampler(SamplerModel sampler) {
    this.sampler = sampler;
    return this;
  }

  @JsonProperty("id_generator")
  @Nullable
  public IdGeneratorModel getIdGenerator() {
    return idGenerator;
  }

  public TracerProviderModel withIdGenerator(IdGeneratorModel idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }

  @JsonProperty("tracer_configurator/development")
  @Nullable
  public ExperimentalTracerConfiguratorModel getTracerConfiguratorDevelopment() {
    return tracerConfiguratorDevelopment;
  }

  public TracerProviderModel withTracerConfiguratorDevelopment(
      ExperimentalTracerConfiguratorModel tracerConfiguratorDevelopment) {
    this.tracerConfiguratorDevelopment = tracerConfiguratorDevelopment;
    return this;
  }

  @Override
  public String toString() {
    return "TracerProviderModel{"
        + "processors="
        + processors
        + ", limits="
        + limits
        + ", sampler="
        + sampler
        + ", idGenerator="
        + idGenerator
        + ", tracerConfiguratorDevelopment="
        + tracerConfiguratorDevelopment
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.processors == null) ? 0 : this.processors.hashCode();
    h *= 1000003;
    h ^= (this.limits == null) ? 0 : this.limits.hashCode();
    h *= 1000003;
    h ^= (this.sampler == null) ? 0 : this.sampler.hashCode();
    h *= 1000003;
    h ^= (this.idGenerator == null) ? 0 : this.idGenerator.hashCode();
    h *= 1000003;
    h ^=
        (this.tracerConfiguratorDevelopment == null)
            ? 0
            : this.tracerConfiguratorDevelopment.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof TracerProviderModel) {
      TracerProviderModel that = (TracerProviderModel) o;
      return (this.processors == null
              ? that.processors == null
              : this.processors.equals(that.processors))
          && (this.limits == null ? that.limits == null : this.limits.equals(that.limits))
          && (this.sampler == null ? that.sampler == null : this.sampler.equals(that.sampler))
          && (this.idGenerator == null
              ? that.idGenerator == null
              : this.idGenerator.equals(that.idGenerator))
          && (this.tracerConfiguratorDevelopment == null
              ? that.tracerConfiguratorDevelopment == null
              : this.tracerConfiguratorDevelopment.equals(that.tracerConfiguratorDevelopment));
    }
    return false;
  }
}
