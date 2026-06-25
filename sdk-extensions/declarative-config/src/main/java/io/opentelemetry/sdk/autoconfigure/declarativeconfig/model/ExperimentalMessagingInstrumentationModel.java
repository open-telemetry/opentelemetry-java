/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"semconv"})
@Generated("jsonschema2pojo")
public class ExperimentalMessagingInstrumentationModel {

  @JsonProperty("semconv")
  @Nullable
  private ExperimentalSemconvConfigModel semconv;

  /**
   * Configure messaging semantic convention version and migration behavior.
   *
   * <p>This property takes precedence over the
   * .instrumentation/development.general.stability_opt_in_list setting.
   *
   * <p>See messaging semantic conventions: https://opentelemetry.io/docs/specs/semconv/messaging/
   *
   * <p>If omitted, uses the general stability_opt_in_list setting, or instrumentations continue
   * emitting their default semantic convention version if not set.
   */
  @JsonProperty("semconv")
  @Nullable
  public ExperimentalSemconvConfigModel getSemconv() {
    return semconv;
  }

  public ExperimentalMessagingInstrumentationModel withSemconv(
      ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalMessagingInstrumentationModel{" + "semconv=" + semconv + "}";
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
    if (o instanceof ExperimentalMessagingInstrumentationModel) {
      ExperimentalMessagingInstrumentationModel that =
          (ExperimentalMessagingInstrumentationModel) o;
      return (this.semconv == null ? that.semconv == null : this.semconv.equals(that.semconv));
    }
    return false;
  }
}
