/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel.CONSOLE;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel.OTLP_GRPC;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel.OTLP_HTTP;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.PushMetricExporterModelAccessor.EXPERIMENTAL_PROPERTIES;

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
public class PushMetricExporterModel {

  static final String OTLP_HTTP = "otlp_http";
  static final String OTLP_GRPC = "otlp_grpc";
  static final String CONSOLE = "console";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(OTLP_HTTP, OtlpHttpMetricExporterModel.class);
    STABLE_PROPERTIES.put(OTLP_GRPC, OtlpGrpcMetricExporterModel.class);
    STABLE_PROPERTIES.put(CONSOLE, ConsoleMetricExporterModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = true;

  @Nullable private OtlpHttpMetricExporterModel otlpHttp;
  @Nullable private OtlpGrpcMetricExporterModel otlpGrpc;
  @Nullable private ConsoleMetricExporterModel console;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure exporter to be OTLP with HTTP transport.
   *
   * <p>If omitted, ignore.
   */
  @JsonProperty(OTLP_HTTP)
  @Nullable
  public OtlpHttpMetricExporterModel getOtlpHttp() {
    if (otlpHttp == null) {
      return ExtensionPropertyUtil.getGraduated(
          OTLP_HTTP, extensionProperties, OtlpHttpMetricExporterModel.class);
    }
    return otlpHttp;
  }

  @JsonProperty(OTLP_HTTP)
  public PushMetricExporterModel withOtlpHttp(OtlpHttpMetricExporterModel otlpHttp) {
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
  public OtlpGrpcMetricExporterModel getOtlpGrpc() {
    if (otlpGrpc == null) {
      return ExtensionPropertyUtil.getGraduated(
          OTLP_GRPC, extensionProperties, OtlpGrpcMetricExporterModel.class);
    }
    return otlpGrpc;
  }

  @JsonProperty(OTLP_GRPC)
  public PushMetricExporterModel withOtlpGrpc(OtlpGrpcMetricExporterModel otlpGrpc) {
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
  public ConsoleMetricExporterModel getConsole() {
    if (console == null) {
      return ExtensionPropertyUtil.getGraduated(
          CONSOLE, extensionProperties, ConsoleMetricExporterModel.class);
    }
    return console;
  }

  @JsonProperty(CONSOLE)
  public PushMetricExporterModel withConsole(ConsoleMetricExporterModel console) {
    this.console = console;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public PushMetricExporterModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "PushMetricExporterModel{"
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
    h ^= (this.getOtlpHttp() == null) ? 0 : this.getOtlpHttp().hashCode();
    h *= 1000003;
    h ^= (this.getOtlpGrpc() == null) ? 0 : this.getOtlpGrpc().hashCode();
    h *= 1000003;
    h ^= (this.getConsole() == null) ? 0 : this.getConsole().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof PushMetricExporterModel) {
      PushMetricExporterModel that = (PushMetricExporterModel) o;
      return (this.getOtlpHttp() == null
              ? that.getOtlpHttp() == null
              : this.getOtlpHttp().equals(that.getOtlpHttp()))
          && (this.getOtlpGrpc() == null
              ? that.getOtlpGrpc() == null
              : this.getOtlpGrpc().equals(that.getOtlpGrpc()))
          && (this.getConsole() == null
              ? that.getConsole() == null
              : this.getConsole().equals(that.getConsole()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
