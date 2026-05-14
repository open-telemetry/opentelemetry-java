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
@JsonPropertyOrder({"semconv", "client", "server"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalHttpInstrumentationModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("semconv")
  private ExperimentalSemconvConfigModel semconv;

  /** (Can be null) */
  @Nullable
  @JsonProperty("client")
  private ExperimentalHttpClientInstrumentationModel client;

  /** (Can be null) */
  @Nullable
  @JsonProperty("server")
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
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalHttpInstrumentationModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("semconv");
    sb.append('=');
    sb.append(((this.semconv == null) ? "<null>" : this.semconv));
    sb.append(',');
    sb.append("client");
    sb.append('=');
    sb.append(((this.client == null) ? "<null>" : this.client));
    sb.append(',');
    sb.append("server");
    sb.append('=');
    sb.append(((this.server == null) ? "<null>" : this.server));
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
    result = ((result * 31) + ((this.client == null) ? 0 : this.client.hashCode()));
    result = ((result * 31) + ((this.server == null) ? 0 : this.server.hashCode()));
    result = ((result * 31) + ((this.semconv == null) ? 0 : this.semconv.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalHttpInstrumentationModel) == false) {
      return false;
    }
    ExperimentalHttpInstrumentationModel rhs = ((ExperimentalHttpInstrumentationModel) other);
    return ((((this.client == rhs.client)
                || ((this.client != null) && this.client.equals(rhs.client)))
            && ((this.server == rhs.server)
                || ((this.server != null) && this.server.equals(rhs.server))))
        && ((this.semconv == rhs.semconv)
            || ((this.semconv != null) && this.semconv.equals(rhs.semconv))));
  }
}
