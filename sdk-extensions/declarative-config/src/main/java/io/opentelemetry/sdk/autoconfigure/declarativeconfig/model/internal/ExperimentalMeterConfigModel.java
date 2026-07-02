/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"enabled"})
@Generated("jsonschema2pojo")
public class ExperimentalMeterConfigModel {

  /** Configure if the meter is enabled or not. If omitted, true is used. */
  @JsonProperty("enabled")
  @JsonPropertyDescription("Configure if the meter is enabled or not.\nIf omitted, true is used.\n")
  @Nullable
  private Boolean enabled;

  /** Configure if the meter is enabled or not. If omitted, true is used. */
  @JsonProperty("enabled")
  @Nullable
  public Boolean getEnabled() {
    return enabled;
  }

  public ExperimentalMeterConfigModel withEnabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalMeterConfigModel{" + "enabled=" + enabled + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.enabled == null) ? 0 : this.enabled.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalMeterConfigModel) {
      ExperimentalMeterConfigModel that = (ExperimentalMeterConfigModel) o;
      return (this.enabled == null ? that.enabled == null : this.enabled.equals(that.enabled));
    }
    return false;
  }
}
