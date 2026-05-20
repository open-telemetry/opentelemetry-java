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
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class PushMetricExporterModel {

  /** (Can be null) */
  @Nullable
  @JsonProperty("otlp_http")
  private OtlpHttpMetricExporterModel otlpHttp;

  /** (Can be null) */
  @Nullable
  @JsonProperty("otlp_grpc")
  private OtlpGrpcMetricExporterModel otlpGrpc;

  /** (Can be null) */
  @Nullable
  @JsonProperty("otlp_file/development")
  private ExperimentalOtlpFileMetricExporterModel otlpFileDevelopment;

  /** (Can be null) */
  @Nullable
  @JsonProperty("console")
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
    StringBuilder sb = new StringBuilder();
    sb.append(PushMetricExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("otlpHttp");
    sb.append('=');
    sb.append(((this.otlpHttp == null) ? "<null>" : this.otlpHttp));
    sb.append(',');
    sb.append("otlpGrpc");
    sb.append('=');
    sb.append(((this.otlpGrpc == null) ? "<null>" : this.otlpGrpc));
    sb.append(',');
    sb.append("otlpFileDevelopment");
    sb.append('=');
    sb.append(((this.otlpFileDevelopment == null) ? "<null>" : this.otlpFileDevelopment));
    sb.append(',');
    sb.append("console");
    sb.append('=');
    sb.append(((this.console == null) ? "<null>" : this.console));
    sb.append(',');
    sb.append("additionalProperties");
    sb.append('=');
    sb.append(((this.additionalProperties == null) ? "<null>" : this.additionalProperties));
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
    result = ((result * 31) + ((this.console == null) ? 0 : this.console.hashCode()));
    result =
        ((result * 31)
            + ((this.otlpFileDevelopment == null) ? 0 : this.otlpFileDevelopment.hashCode()));
    result = ((result * 31) + ((this.otlpGrpc == null) ? 0 : this.otlpGrpc.hashCode()));
    result =
        ((result * 31)
            + ((this.additionalProperties == null) ? 0 : this.additionalProperties.hashCode()));
    result = ((result * 31) + ((this.otlpHttp == null) ? 0 : this.otlpHttp.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof PushMetricExporterModel) == false) {
      return false;
    }
    PushMetricExporterModel rhs = ((PushMetricExporterModel) other);
    return ((((((this.console == rhs.console)
                        || ((this.console != null) && this.console.equals(rhs.console)))
                    && ((this.otlpFileDevelopment == rhs.otlpFileDevelopment)
                        || ((this.otlpFileDevelopment != null)
                            && this.otlpFileDevelopment.equals(rhs.otlpFileDevelopment))))
                && ((this.otlpGrpc == rhs.otlpGrpc)
                    || ((this.otlpGrpc != null) && this.otlpGrpc.equals(rhs.otlpGrpc))))
            && ((this.additionalProperties == rhs.additionalProperties)
                || ((this.additionalProperties != null)
                    && this.additionalProperties.equals(rhs.additionalProperties))))
        && ((this.otlpHttp == rhs.otlpHttp)
            || ((this.otlpHttp != null) && this.otlpHttp.equals(rhs.otlpHttp))));
  }
}
