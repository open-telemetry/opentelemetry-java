/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfiguratorModel;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"readers", "views", "exemplar_filter", "meter_configurator/development"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class MeterProviderModel {

  @Nullable private List<MetricReaderModel> readers;
  @Nullable private List<ViewModel> views;
  @Nullable private ExemplarFilterModel exemplarFilter;
  @Nullable private ExperimentalMeterConfiguratorModel meterConfiguratorDevelopment;

  /**
   * Configure metric readers.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("readers")
  @Nullable
  public List<MetricReaderModel> getReaders() {
    return readers;
  }

  @JsonProperty("readers")
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
  @JsonProperty("views")
  @Nullable
  public List<ViewModel> getViews() {
    return views;
  }

  @JsonProperty("views")
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
  @JsonProperty("exemplar_filter")
  @Nullable
  public ExemplarFilterModel getExemplarFilter() {
    return exemplarFilter;
  }

  @JsonProperty("exemplar_filter")
  public MeterProviderModel withExemplarFilter(ExemplarFilterModel exemplarFilter) {
    this.exemplarFilter = exemplarFilter;
    return this;
  }

  /**
   * Configure meters.
   *
   * <p>If omitted, all meters use default values as described in ExperimentalMeterConfig.
   */
  @JsonProperty("meter_configurator/development")
  @Nullable
  public ExperimentalMeterConfiguratorModel getMeterConfiguratorDevelopment() {
    return meterConfiguratorDevelopment;
  }

  @JsonProperty("meter_configurator/development")
  public MeterProviderModel withMeterConfiguratorDevelopment(
      ExperimentalMeterConfiguratorModel meterConfiguratorDevelopment) {
    this.meterConfiguratorDevelopment = meterConfiguratorDevelopment;
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
        + ", meterConfiguratorDevelopment="
        + meterConfiguratorDevelopment
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
    h ^=
        (this.meterConfiguratorDevelopment == null)
            ? 0
            : this.meterConfiguratorDevelopment.hashCode();
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
          && (this.meterConfiguratorDevelopment == null
              ? that.meterConfiguratorDevelopment == null
              : this.meterConfiguratorDevelopment.equals(that.meterConfiguratorDevelopment));
    }
    return false;
  }
}
