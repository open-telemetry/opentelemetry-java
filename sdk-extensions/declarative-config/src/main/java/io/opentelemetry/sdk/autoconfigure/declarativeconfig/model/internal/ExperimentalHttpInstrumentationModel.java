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
@JsonPropertyOrder({"semconv", "client", "server"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class ExperimentalHttpInstrumentationModel {

  @Nullable private ExperimentalSemconvConfigModel semconv;
  @Nullable private ExperimentalHttpClientInstrumentationModel client;
  @Nullable private ExperimentalHttpServerInstrumentationModel server;

  /**
   * Configure HTTP semantic convention version and migration behavior.
   *
   * <p>This property takes precedence over the
   * .instrumentation/development.general.stability_opt_in_list setting.
   *
   * <p>See HTTP migration:
   * https://opentelemetry.io/docs/specs/semconv/non-normative/http-migration/
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
  public ExperimentalHttpInstrumentationModel withSemconv(ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  /**
   * Configure instrumentations following the http client semantic conventions.
   *
   * <p>If omitted, defaults as described in ExperimentalHttpClientInstrumentation are used.
   */
  @JsonProperty("client")
  @Nullable
  public ExperimentalHttpClientInstrumentationModel getClient() {
    return client;
  }

  @JsonProperty("client")
  public ExperimentalHttpInstrumentationModel withClient(
      ExperimentalHttpClientInstrumentationModel client) {
    this.client = client;
    return this;
  }

  /**
   * Configure instrumentations following the http server semantic conventions.
   *
   * <p>If omitted, defaults as described in ExperimentalHttpServerInstrumentation are used.
   */
  @JsonProperty("server")
  @Nullable
  public ExperimentalHttpServerInstrumentationModel getServer() {
    return server;
  }

  @JsonProperty("server")
  public ExperimentalHttpInstrumentationModel withServer(
      ExperimentalHttpServerInstrumentationModel server) {
    this.server = server;
    return this;
  }

  @Override
  public String toString() {
    return "ExperimentalHttpInstrumentationModel{"
        + "semconv="
        + semconv
        + ", client="
        + client
        + ", server="
        + server
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.semconv == null) ? 0 : this.semconv.hashCode();
    h *= 1000003;
    h ^= (this.client == null) ? 0 : this.client.hashCode();
    h *= 1000003;
    h ^= (this.server == null) ? 0 : this.server.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalHttpInstrumentationModel) {
      ExperimentalHttpInstrumentationModel that = (ExperimentalHttpInstrumentationModel) o;
      return (this.semconv == null ? that.semconv == null : this.semconv.equals(that.semconv))
          && (this.client == null ? that.client == null : this.client.equals(that.client))
          && (this.server == null ? that.server == null : this.server.equals(that.server));
    }
    return false;
  }
}
