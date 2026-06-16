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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"readers", "views", "exemplar_filter", "meter_configurator/development"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class MeterProviderModel {

  /**
   * Configure metric readers. Property is required and must be non-null.
   *
   * <p>(Required)
   */
  @JsonProperty("readers")
  @JsonPropertyDescription(
      "Configure metric readers.\nProperty is required and must be non-null.\n")
  @Nonnull
  private List<MetricReaderModel> readers;

  /**
   * Configure views. Each view has a selector which determines the instrument(s) it applies to, and
   * a configuration for the resulting stream(s). If omitted, no views are registered.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("views")
  @JsonPropertyDescription(
      "Configure views. \nEach view has a selector which determines the instrument(s) it applies to, and a configuration for the resulting stream(s).\nIf omitted, no views are registered.\n")
  private List<ViewModel> views;

  /** (Can be null) */
  @Nullable
  @JsonProperty("exemplar_filter")
  private MeterProviderModel.ExemplarFilter exemplarFilter;

  /** (Can be null) */
  @Nullable
  @JsonProperty("meter_configurator/development")
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
    StringBuilder sb = new StringBuilder();
    sb.append(MeterProviderModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("readers");
    sb.append('=');
    sb.append(((this.readers == null) ? "<null>" : this.readers));
    sb.append(',');
    sb.append("views");
    sb.append('=');
    sb.append(((this.views == null) ? "<null>" : this.views));
    sb.append(',');
    sb.append("exemplarFilter");
    sb.append('=');
    sb.append(((this.exemplarFilter == null) ? "<null>" : this.exemplarFilter));
    sb.append(',');
    sb.append("meterConfiguratorDevelopment");
    sb.append('=');
    sb.append(
        ((this.meterConfiguratorDevelopment == null)
            ? "<null>"
            : this.meterConfiguratorDevelopment));
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
    result = ((result * 31) + ((this.exemplarFilter == null) ? 0 : this.exemplarFilter.hashCode()));
    result =
        ((result * 31)
            + ((this.meterConfiguratorDevelopment == null)
                ? 0
                : this.meterConfiguratorDevelopment.hashCode()));
    result = ((result * 31) + ((this.readers == null) ? 0 : this.readers.hashCode()));
    result = ((result * 31) + ((this.views == null) ? 0 : this.views.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof MeterProviderModel) == false) {
      return false;
    }
    MeterProviderModel rhs = ((MeterProviderModel) other);
    return (((((this.exemplarFilter == rhs.exemplarFilter)
                    || ((this.exemplarFilter != null)
                        && this.exemplarFilter.equals(rhs.exemplarFilter)))
                && ((this.meterConfiguratorDevelopment == rhs.meterConfiguratorDevelopment)
                    || ((this.meterConfiguratorDevelopment != null)
                        && this.meterConfiguratorDevelopment.equals(
                            rhs.meterConfiguratorDevelopment))))
            && ((this.readers == rhs.readers)
                || ((this.readers != null) && this.readers.equals(rhs.readers))))
        && ((this.views == rhs.views) || ((this.views != null) && this.views.equals(rhs.views))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
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
