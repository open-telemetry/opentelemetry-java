/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel.CONSOLE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel.OTLP_GRPC;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.SpanExporterModel.OTLP_HTTP;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.SpanExporterModelAccessor.EXPERIMENTAL_PROPERTIES;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({OTLP_HTTP, OTLP_GRPC, CONSOLE})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class SpanExporterModel {

  static final String OTLP_HTTP = "otlp_http";
  static final String OTLP_GRPC = "otlp_grpc";
  static final String CONSOLE = "console";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(OTLP_HTTP, OtlpHttpExporterModel.class);
    STABLE_PROPERTIES.put(OTLP_GRPC, OtlpGrpcExporterModel.class);
    STABLE_PROPERTIES.put(CONSOLE, ConsoleExporterModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private OtlpHttpExporterModel otlpHttp;
  @Nullable private OtlpGrpcExporterModel otlpGrpc;
  @Nullable private ConsoleExporterModel console;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure exporter to be OTLP with HTTP transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(OTLP_HTTP)
  @Nullable
  public OtlpHttpExporterModel getOtlpHttp() {
    if (otlpHttp == null) {
      return ExtensionPropertyUtil.getGraduated(
          OTLP_HTTP, extensionProperties, OtlpHttpExporterModel.class);
    }
    return otlpHttp;
  }

  @JsonProperty(OTLP_HTTP)
  public SpanExporterModel withOtlpHttp(OtlpHttpExporterModel otlpHttp) {
    this.otlpHttp = otlpHttp;
    return this;
  }

  /**
   * Configure exporter to be OTLP with gRPC transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(OTLP_GRPC)
  @Nullable
  public OtlpGrpcExporterModel getOtlpGrpc() {
    if (otlpGrpc == null) {
      return ExtensionPropertyUtil.getGraduated(
          OTLP_GRPC, extensionProperties, OtlpGrpcExporterModel.class);
    }
    return otlpGrpc;
  }

  @JsonProperty(OTLP_GRPC)
  public SpanExporterModel withOtlpGrpc(OtlpGrpcExporterModel otlpGrpc) {
    this.otlpGrpc = otlpGrpc;
    return this;
  }

  /**
   * Configure exporter to be console.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(CONSOLE)
  @Nullable
  public ConsoleExporterModel getConsole() {
    if (console == null) {
      return ExtensionPropertyUtil.getGraduated(
          CONSOLE, extensionProperties, ConsoleExporterModel.class);
    }
    return console;
  }

  @JsonProperty(CONSOLE)
  public SpanExporterModel withConsole(ConsoleExporterModel console) {
    this.console = console;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public SpanExporterModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        EXPERIMENTAL_PROPERTIES,
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "SpanExporterModel{"
        + "otlpHttp="
        + otlpHttp
        + ", otlpGrpc="
        + otlpGrpc
        + ", console="
        + console
        + ", extensionProperties="
        + extensionProperties
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
    h ^= (this.console == null) ? 0 : this.console.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanExporterModel) {
      SpanExporterModel that = (SpanExporterModel) o;
      return (this.otlpHttp == null ? that.otlpHttp == null : this.otlpHttp.equals(that.otlpHttp))
          && (this.otlpGrpc == null ? that.otlpGrpc == null : this.otlpGrpc.equals(that.otlpGrpc))
          && (this.console == null ? that.console == null : this.console.equals(that.console))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
