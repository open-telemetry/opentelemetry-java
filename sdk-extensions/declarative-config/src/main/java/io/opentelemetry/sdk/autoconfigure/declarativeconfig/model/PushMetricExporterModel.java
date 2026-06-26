/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExperimentalOtlpFileMetricExporterModel;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"otlp_http", "otlp_grpc", "otlp_file/development", "console"})
@Generated("jsonschema2pojo")
public class PushMetricExporterModel {

  @JsonProperty("otlp_http")
  @Nullable
  private OtlpHttpMetricExporterModel otlpHttp;

  @JsonProperty("otlp_grpc")
  @Nullable
  private OtlpGrpcMetricExporterModel otlpGrpc;

  @JsonProperty("otlp_file/development")
  @Nullable
  private ExperimentalOtlpFileMetricExporterModel otlpFileDevelopment;

  @JsonProperty("console")
  @Nullable
  private ConsoleMetricExporterModel console;

  @JsonIgnore
  private Map<String, PushMetricExporterPropertyModel> additionalProperties =
      new LinkedHashMap<String, PushMetricExporterPropertyModel>();

  @JsonProperty("otlp_http")
  @Nullable
  public OtlpHttpMetricExporterModel getOtlpHttp() {
    return otlpHttp;
  }

  public PushMetricExporterModel withOtlpHttp(OtlpHttpMetricExporterModel otlpHttp) {
    this.otlpHttp = otlpHttp;
    return this;
  }

  @JsonProperty("otlp_grpc")
  @Nullable
  public OtlpGrpcMetricExporterModel getOtlpGrpc() {
    return otlpGrpc;
  }

  public PushMetricExporterModel withOtlpGrpc(OtlpGrpcMetricExporterModel otlpGrpc) {
    this.otlpGrpc = otlpGrpc;
    return this;
  }

  @JsonProperty("otlp_file/development")
  @Nullable
  public ExperimentalOtlpFileMetricExporterModel getOtlpFileDevelopment() {
    return otlpFileDevelopment;
  }

  public PushMetricExporterModel withOtlpFileDevelopment(
      ExperimentalOtlpFileMetricExporterModel otlpFileDevelopment) {
    this.otlpFileDevelopment = otlpFileDevelopment;
    return this;
  }

  @JsonProperty("console")
  @Nullable
  public ConsoleMetricExporterModel getConsole() {
    return console;
  }

  public PushMetricExporterModel withConsole(ConsoleMetricExporterModel console) {
    this.console = console;
    return this;
  }

  @JsonAnyGetter
  public Map<String, PushMetricExporterPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, PushMetricExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public PushMetricExporterModel withAdditionalProperty(
      String name, PushMetricExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "PushMetricExporterModel{"
        + "otlpHttp="
        + otlpHttp
        + ", otlpGrpc="
        + otlpGrpc
        + ", otlpFileDevelopment="
        + otlpFileDevelopment
        + ", console="
        + console
        + ", additionalProperties="
        + additionalProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.otlpHttp == null) ? 0 : this.otlpHttp.hashCode();
    h *= 1000003;
    h ^= (this.otlpGrpc == null) ? 0 : this.otlpGrpc.hashCode();
    h *= 1000003;
    h ^= (this.otlpFileDevelopment == null) ? 0 : this.otlpFileDevelopment.hashCode();
    h *= 1000003;
    h ^= (this.console == null) ? 0 : this.console.hashCode();
    h *= 1000003;
    h ^= (this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PushMetricExporterModel) {
      PushMetricExporterModel that = (PushMetricExporterModel) o;
      return (this.otlpHttp == null ? that.otlpHttp == null : this.otlpHttp.equals(that.otlpHttp))
          && (this.otlpGrpc == null ? that.otlpGrpc == null : this.otlpGrpc.equals(that.otlpGrpc))
          && (this.otlpFileDevelopment == null
              ? that.otlpFileDevelopment == null
              : this.otlpFileDevelopment.equals(that.otlpFileDevelopment))
          && (this.console == null ? that.console == null : this.console.equals(that.console))
          && (this.additionalProperties == null
              ? that.additionalProperties == null
              : this.additionalProperties.equals(that.additionalProperties));
    }
    return false;
  }
}
