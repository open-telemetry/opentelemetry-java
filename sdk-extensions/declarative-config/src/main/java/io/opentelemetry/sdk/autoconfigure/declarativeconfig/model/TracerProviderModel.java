/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel.ID_GENERATOR;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel.LIMITS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel.PROCESSORS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.TracerProviderModel.SAMPLER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.TracerProviderModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({PROCESSORS, LIMITS, SAMPLER, ID_GENERATOR})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class TracerProviderModel {

  static final String PROCESSORS = "processors";
  static final String LIMITS = "limits";
  static final String SAMPLER = "sampler";
  static final String ID_GENERATOR = "id_generator";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(LIMITS, SpanLimitsModel.class);
    STABLE_PROPERTIES.put(SAMPLER, SamplerModel.class);
    STABLE_PROPERTIES.put(ID_GENERATOR, IdGeneratorModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<SpanProcessorModel> processors;
  @Nullable private SpanLimitsModel limits;
  @Nullable private SamplerModel sampler;
  @Nullable private IdGeneratorModel idGenerator;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure span processors.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(PROCESSORS)
  @Nullable
  public List<SpanProcessorModel> getProcessors() {
    return processors;
  }

  @JsonProperty(PROCESSORS)
  public TracerProviderModel withProcessors(List<SpanProcessorModel> processors) {
    this.processors = processors;
    return this;
  }

  /**
   * Configure span limits. See also attribute_limits.
   *
   * <p>If omitted, default values as described in SpanLimits are used.
   */
  @JsonProperty(LIMITS)
  @Nullable
  public SpanLimitsModel getLimits() {
    if (limits == null) {
      return ExtensionPropertyUtil.getGraduated(LIMITS, extensionProperties, SpanLimitsModel.class);
    }
    return limits;
  }

  @JsonProperty(LIMITS)
  public TracerProviderModel withLimits(SpanLimitsModel limits) {
    this.limits = limits;
    return this;
  }

  /**
   * Configure the sampler.
   *
   * <p>If omitted, parent based sampler with a root of always_on is used.
   */
  @JsonProperty(SAMPLER)
  @Nullable
  public SamplerModel getSampler() {
    if (sampler == null) {
      return ExtensionPropertyUtil.getGraduated(SAMPLER, extensionProperties, SamplerModel.class);
    }
    return sampler;
  }

  @JsonProperty(SAMPLER)
  public TracerProviderModel withSampler(SamplerModel sampler) {
    this.sampler = sampler;
    return this;
  }

  /**
   * Configure the trace and span ID generator.
   *
   * <p>If omitted, RandomIdGenerator is used.
   */
  @JsonProperty(ID_GENERATOR)
  @Nullable
  public IdGeneratorModel getIdGenerator() {
    if (idGenerator == null) {
      return ExtensionPropertyUtil.getGraduated(
          ID_GENERATOR, extensionProperties, IdGeneratorModel.class);
    }
    return idGenerator;
  }

  @JsonProperty(ID_GENERATOR)
  public TracerProviderModel withIdGenerator(IdGeneratorModel idGenerator) {
    this.idGenerator = idGenerator;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public TracerProviderModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "TracerProviderModel{"
        + "processors="
        + processors
        + ", limits="
        + limits
        + ", sampler="
        + sampler
        + ", idGenerator="
        + idGenerator
        + ", extensionProperties="
        + extensionProperties
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
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
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
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
