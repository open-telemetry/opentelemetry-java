/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalMeterConfiguratorModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"readers", "views", "exemplar_filter", "meter_configurator/development"})
@Generated("jsonschema2pojo")
public class MeterProviderModel {

  /**
   * Configure metric readers. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("readers")
  @JsonPropertyDescription(
      "Configure metric readers.\nProperty is required and must be non-null.\n")
  @Nullable
  private List<MetricReaderModel> readers;

  /**
   * Configure views. Each view has a selector which determines the instrument(s) it applies to, and
   * a configuration for the resulting stream(s). If omitted, no views are registered.
   */
  @JsonProperty("views")
  @JsonPropertyDescription(
      "Configure views. \nEach view has a selector which determines the instrument(s) it applies to, and a configuration for the resulting stream(s).\nIf omitted, no views are registered.\n")
  @Nullable
  private List<ViewModel> views;

  @JsonProperty("exemplar_filter")
  @Nullable
  private MeterProviderModel.ExemplarFilter exemplarFilter;

  @JsonProperty("meter_configurator/development")
  @Nullable
  private ExperimentalMeterConfiguratorModel meterConfiguratorDevelopment;

  /**
   * Configure metric readers. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("readers")
  @Nullable
  public List<MetricReaderModel> getReaders() {
    return readers;
  }

  public MeterProviderModel withReaders(List<MetricReaderModel> readers) {
    this.readers = readers;
    return this;
  }

  /**
   * Configure views. Each view has a selector which determines the instrument(s) it applies to, and
   * a configuration for the resulting stream(s). If omitted, no views are registered.
   */
  @JsonProperty("views")
  @Nullable
  public List<ViewModel> getViews() {
    return views;
  }

  public MeterProviderModel withViews(List<ViewModel> views) {
    this.views = views;
    return this;
  }

  @JsonProperty("exemplar_filter")
  @Nullable
  public MeterProviderModel.ExemplarFilter getExemplarFilter() {
    return exemplarFilter;
  }

  public MeterProviderModel withExemplarFilter(MeterProviderModel.ExemplarFilter exemplarFilter) {
    this.exemplarFilter = exemplarFilter;
    return this;
  }

  @JsonProperty("meter_configurator/development")
  @Nullable
  public ExperimentalMeterConfiguratorModel getMeterConfiguratorDevelopment() {
    return meterConfiguratorDevelopment;
  }

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

  @Generated("jsonschema2pojo")
  public enum ExemplarFilter {
    ALWAYS_ON("always_on"),
    ALWAYS_OFF("always_off"),
    TRACE_BASED("trace_based");
    private final String value;
    private static final Map<String, MeterProviderModel.ExemplarFilter> CONSTANTS =
        new HashMap<String, MeterProviderModel.ExemplarFilter>();

    static {
      for (MeterProviderModel.ExemplarFilter c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ExemplarFilter(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static MeterProviderModel.ExemplarFilter fromValue(String value) {
      MeterProviderModel.ExemplarFilter constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
