/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel.EXEMPLAR_FILTER;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel.READERS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel.VIEWS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.MeterProviderModelAccessor.EXPERIMENTAL_PROPERTIES;

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
@JsonPropertyOrder({READERS, VIEWS, EXEMPLAR_FILTER})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class MeterProviderModel {

  static final String READERS = "readers";
  static final String VIEWS = "views";
  static final String EXEMPLAR_FILTER = "exemplar_filter";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(EXEMPLAR_FILTER, ExemplarFilterModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private List<MetricReaderModel> readers;
  @Nullable private List<ViewModel> views;
  @Nullable private ExemplarFilterModel exemplarFilter;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure metric readers.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty(READERS)
  @Nullable
  public List<MetricReaderModel> getReaders() {
    return readers;
  }

  @JsonProperty(READERS)
  public MeterProviderModel withReaders(List<MetricReaderModel> readers) {
    this.readers = readers;
    return this;
  }

  /**
   * Configure views.
   *
   * <p>Each view has a selector which determines the instrument(s) it applies to, and a
   * configuration for the resulting stream(s).
   *
   * <p>If omitted, no views are registered.
   */
  @JsonProperty(VIEWS)
  @Nullable
  public List<ViewModel> getViews() {
    return views;
  }

  @JsonProperty(VIEWS)
  public MeterProviderModel withViews(List<ViewModel> views) {
    this.views = views;
    return this;
  }

  /**
   * Configure the exemplar filter.
   *
   * <p>Values include:
   *
   * <p>* always_off: ExemplarFilter which makes no measurements eligible for being an Exemplar.
   *
   * <p>* always_on: ExemplarFilter which makes all measurements eligible for being an Exemplar.
   *
   * <p>* trace_based: ExemplarFilter which makes measurements recorded in the context of a sampled
   * parent span eligible for being an Exemplar.
   *
   * <p>If omitted, trace_based is used.
   */
  @JsonProperty(EXEMPLAR_FILTER)
  @Nullable
  public ExemplarFilterModel getExemplarFilter() {
    if (exemplarFilter == null) {
      return ExtensionPropertyUtil.getGraduated(
          EXEMPLAR_FILTER, extensionProperties, ExemplarFilterModel.class);
    }
    return exemplarFilter;
  }

  @JsonProperty(EXEMPLAR_FILTER)
  public MeterProviderModel withExemplarFilter(ExemplarFilterModel exemplarFilter) {
    this.exemplarFilter = exemplarFilter;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public MeterProviderModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "MeterProviderModel{"
        + "readers="
        + readers
        + ", views="
        + views
        + ", exemplarFilter="
        + exemplarFilter
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.readers == null) ? 0 : this.readers.hashCode();
    h *= 1000003;
    h ^= (this.views == null) ? 0 : this.views.hashCode();
    h *= 1000003;
    h ^= (this.exemplarFilter == null) ? 0 : this.exemplarFilter.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof MeterProviderModel) {
      MeterProviderModel that = (MeterProviderModel) o;
      return (this.readers == null ? that.readers == null : this.readers.equals(that.readers))
          && (this.views == null ? that.views == null : this.views.equals(that.views))
          && (this.exemplarFilter == null
              ? that.exemplarFilter == null
              : this.exemplarFilter.equals(that.exemplarFilter))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
