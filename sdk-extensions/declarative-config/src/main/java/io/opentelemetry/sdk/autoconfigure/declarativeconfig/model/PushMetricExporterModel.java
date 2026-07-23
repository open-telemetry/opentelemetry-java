/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class PushMetricExporterModel {

  @Nullable private OtlpHttpMetricExporterModel otlpHttp;
  @Nullable private OtlpGrpcMetricExporterModel otlpGrpc;
  @Nullable private ExperimentalOtlpFileMetricExporterModel otlpFileDevelopment;
  @Nullable private ConsoleMetricExporterModel console;
  private Map<String, PushMetricExporterPropertyModel> additionalProperties =
      new LinkedHashMap<String, PushMetricExporterPropertyModel>();

  /**
   * Configure exporter to be OTLP with HTTP transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("otlp_http")
  @Nullable
  public OtlpHttpMetricExporterModel getOtlpHttp() {
    return otlpHttp;
  }

  @JsonProperty("otlp_http")
  public PushMetricExporterModel withOtlpHttp(OtlpHttpMetricExporterModel otlpHttp) {
    this.otlpHttp = otlpHttp;
    return this;
  }

  /**
   * Configure exporter to be OTLP with gRPC transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("otlp_grpc")
  @Nullable
  public OtlpGrpcMetricExporterModel getOtlpGrpc() {
    return otlpGrpc;
  }

  @JsonProperty("otlp_grpc")
  public PushMetricExporterModel withOtlpGrpc(OtlpGrpcMetricExporterModel otlpGrpc) {
    this.otlpGrpc = otlpGrpc;
    return this;
  }

  /**
   * Configure exporter to be OTLP with file transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("otlp_file/development")
  @Nullable
  public ExperimentalOtlpFileMetricExporterModel getOtlpFileDevelopment() {
    return otlpFileDevelopment;
  }

  @JsonProperty("otlp_file/development")
  public PushMetricExporterModel withOtlpFileDevelopment(
      ExperimentalOtlpFileMetricExporterModel otlpFileDevelopment) {
    this.otlpFileDevelopment = otlpFileDevelopment;
    return this;
  }

  /**
   * Configure exporter to be console.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("console")
  @Nullable
  public ConsoleMetricExporterModel getConsole() {
    return console;
  }

  @JsonProperty("console")
  public PushMetricExporterModel withConsole(ConsoleMetricExporterModel console) {
    this.console = console;
    return this;
  }

  @JsonAnyGetter
  public Map<String, PushMetricExporterPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
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
