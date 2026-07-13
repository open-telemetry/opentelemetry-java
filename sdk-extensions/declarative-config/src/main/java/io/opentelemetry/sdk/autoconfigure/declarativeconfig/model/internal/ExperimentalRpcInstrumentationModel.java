/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"semconv"})
@Generated("jsonschema2pojo")
public class ExperimentalRpcInstrumentationModel {

  @Nullable private ExperimentalSemconvConfigModel semconv;

  /**
   * Configure RPC semantic convention version and migration behavior.
   *
   * <p>This property takes precedence over the
   * .instrumentation/development.general.stability_opt_in_list setting.
   *
   * <p>See RPC semantic conventions: https://opentelemetry.io/docs/specs/semconv/rpc/
   *
   * <p>If omitted, uses the general stability_opt_in_list setting, or instrumentations continue
   * emitting their default semantic convention version if not set.
   */
  @JsonProperty("semconv")
  @Nullable
  public ExperimentalSemconvConfigModel getSemconv() {
    return semconv;
  }

  @JsonProperty("semconv")
  public ExperimentalRpcInstrumentationModel withSemconv(ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalRpcInstrumentationModel{" + "semconv=" + semconv + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.semconv == null) ? 0 : this.semconv.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalRpcInstrumentationModel) {
      ExperimentalRpcInstrumentationModel that = (ExperimentalRpcInstrumentationModel) o;
      return (this.semconv == null ? that.semconv == null : this.semconv.equals(that.semconv));
    }
    return false;
  }
}
