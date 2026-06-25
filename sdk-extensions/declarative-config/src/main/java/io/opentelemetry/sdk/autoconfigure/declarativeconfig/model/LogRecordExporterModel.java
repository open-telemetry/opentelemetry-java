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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"otlp_http", "otlp_grpc", "otlp_file/development", "console"})
@Generated("jsonschema2pojo")
public class LogRecordExporterModel {

  @JsonProperty("otlp_http")
  @Nullable
  private OtlpHttpExporterModel otlpHttp;

  @JsonProperty("otlp_grpc")
  @Nullable
  private OtlpGrpcExporterModel otlpGrpc;

  @JsonProperty("otlp_file/development")
  @Nullable
  private ExperimentalOtlpFileExporterModel otlpFileDevelopment;

  @JsonProperty("console")
  @Nullable
  private ConsoleExporterModel console;

  @JsonIgnore
  private Map<String, LogRecordExporterPropertyModel> additionalProperties =
      new LinkedHashMap<String, LogRecordExporterPropertyModel>();

  /**
   * Configure exporter to be OTLP with HTTP transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty("otlp_http")
  @Nullable
  public OtlpHttpExporterModel getOtlpHttp() {
    return otlpHttp;
  }

  public LogRecordExporterModel withOtlpHttp(OtlpHttpExporterModel otlpHttp) {
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
  public OtlpGrpcExporterModel getOtlpGrpc() {
    return otlpGrpc;
  }

  public LogRecordExporterModel withOtlpGrpc(OtlpGrpcExporterModel otlpGrpc) {
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
  public ExperimentalOtlpFileExporterModel getOtlpFileDevelopment() {
    return otlpFileDevelopment;
  }

  public LogRecordExporterModel withOtlpFileDevelopment(
      ExperimentalOtlpFileExporterModel otlpFileDevelopment) {
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
  public ConsoleExporterModel getConsole() {
    return console;
  }

  public LogRecordExporterModel withConsole(ConsoleExporterModel console) {
    this.console = console;
    return this;
  }

  @JsonAnyGetter
  public Map<String, LogRecordExporterPropertyModel> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, LogRecordExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
  }

  public LogRecordExporterModel withAdditionalProperty(
      String name, LogRecordExporterPropertyModel value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override
  public String toString() {
    return "LogRecordExporterModel{"
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
    if (o instanceof LogRecordExporterModel) {
      LogRecordExporterModel that = (LogRecordExporterModel) o;
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
