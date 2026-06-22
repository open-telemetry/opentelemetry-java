/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class TracerProviderModel {

  /**
   * Configure span processors. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("processors")
  @JsonPropertyDescription(
      "Configure span processors.\nProperty is required and must be non-null.\n")
  @Nonnull
  private List<SpanProcessorModel> processors;

  /** (Can be null) */
  @Nullable
  @JsonProperty("limits")
  private SpanLimitsModel limits;

  /** (Can be null) */
  @Nullable
  @JsonProperty("sampler")
  private SamplerModel sampler;

  /** (Can be null) */
  @Nullable
  @JsonProperty("id_generator")
  private IdGeneratorModel idGenerator;

  /** (Can be null) */
  @Nullable
  @JsonProperty("tracer_configurator/development")
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
    StringBuilder sb = new StringBuilder();
    sb.append(TracerProviderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("processors");
    sb.append('=');
    sb.append(((this.processors == null) ? "<null>" : this.processors));
    sb.append(',');
    sb.append("limits");
    sb.append('=');
    sb.append(((this.limits == null) ? "<null>" : this.limits));
    sb.append(',');
    sb.append("sampler");
    sb.append('=');
    sb.append(((this.sampler == null) ? "<null>" : this.sampler));
    sb.append(',');
    sb.append("idGenerator");
    sb.append('=');
    sb.append(((this.idGenerator == null) ? "<null>" : this.idGenerator));
    sb.append(',');
    sb.append("tracerConfiguratorDevelopment");
    sb.append('=');
    sb.append(
        ((this.tracerConfiguratorDevelopment == null)
            ? "<null>"
            : this.tracerConfiguratorDevelopment));
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
    result = ((result * 31) + ((this.idGenerator == null) ? 0 : this.idGenerator.hashCode()));
    result =
        ((result * 31)
            + ((this.tracerConfiguratorDevelopment == null)
                ? 0
                : this.tracerConfiguratorDevelopment.hashCode()));
    result = ((result * 31) + ((this.processors == null) ? 0 : this.processors.hashCode()));
    result = ((result * 31) + ((this.limits == null) ? 0 : this.limits.hashCode()));
    result = ((result * 31) + ((this.sampler == null) ? 0 : this.sampler.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof TracerProviderModel) == false) {
      return false;
    }
    TracerProviderModel rhs = ((TracerProviderModel) other);
    return ((((((this.idGenerator == rhs.idGenerator)
                        || ((this.idGenerator != null) && this.idGenerator.equals(rhs.idGenerator)))
                    && ((this.tracerConfiguratorDevelopment == rhs.tracerConfiguratorDevelopment)
                        || ((this.tracerConfiguratorDevelopment != null)
                            && this.tracerConfiguratorDevelopment.equals(
                                rhs.tracerConfiguratorDevelopment))))
                && ((this.processors == rhs.processors)
                    || ((this.processors != null) && this.processors.equals(rhs.processors))))
            && ((this.limits == rhs.limits)
                || ((this.limits != null) && this.limits.equals(rhs.limits))))
        && ((this.sampler == rhs.sampler)
            || ((this.sampler != null) && this.sampler.equals(rhs.sampler))));
  }
}
