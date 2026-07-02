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
@Generated("jsonschema2pojo")
public class ExperimentalHttpInstrumentationModel {

  @JsonProperty("semconv")
  @Nullable
  private ExperimentalSemconvConfigModel semconv;

  @JsonProperty("client")
  @Nullable
  private ExperimentalHttpClientInstrumentationModel client;

  @JsonProperty("server")
  @Nullable
  private ExperimentalHttpServerInstrumentationModel server;

  @JsonProperty("semconv")
  @Nullable
  public ExperimentalSemconvConfigModel getSemconv() {
    return semconv;
  }

  public ExperimentalHttpInstrumentationModel withSemconv(ExperimentalSemconvConfigModel semconv) {
    this.semconv = semconv;
    return this;
  }

  @JsonProperty("client")
  @Nullable
  public ExperimentalHttpClientInstrumentationModel getClient() {
    return client;
  }

  public ExperimentalHttpInstrumentationModel withClient(
      ExperimentalHttpClientInstrumentationModel client) {
    this.client = client;
    return this;
  }

  @JsonProperty("server")
  @Nullable
  public ExperimentalHttpServerInstrumentationModel getServer() {
    return server;
  }

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
