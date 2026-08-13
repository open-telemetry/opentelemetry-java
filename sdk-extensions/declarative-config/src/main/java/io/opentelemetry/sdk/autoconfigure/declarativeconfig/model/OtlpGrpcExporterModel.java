/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.COMPRESSION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.ENDPOINT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.HEADERS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.HEADERS_LIST;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.TIMEOUT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel.TLS;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal.ExtensionPropertyUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ENDPOINT, TLS, HEADERS, HEADERS_LIST, COMPRESSION, TIMEOUT})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class OtlpGrpcExporterModel {

  static final String ENDPOINT = "endpoint";
  static final String TLS = "tls";
  static final String HEADERS = "headers";
  static final String HEADERS_LIST = "headers_list";
  static final String COMPRESSION = "compression";
  static final String TIMEOUT = "timeout";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ENDPOINT, String.class);
    STABLE_PROPERTIES.put(TLS, GrpcTlsModel.class);
    STABLE_PROPERTIES.put(HEADERS_LIST, String.class);
    STABLE_PROPERTIES.put(COMPRESSION, String.class);
    STABLE_PROPERTIES.put(TIMEOUT, Integer.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String endpoint;
  @Nullable private GrpcTlsModel tls;
  @Nullable private List<NameStringValuePairModel> headers;
  @Nullable private String headersList;
  @Nullable private String compression;
  @Nullable private Integer timeout;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure endpoint.
   *
   * <p>If omitted or null, http://localhost:4317 is used.
   */
  @JsonProperty(ENDPOINT)
  @Nullable
  public String getEndpoint() {
    if (endpoint == null) {
      return ExtensionPropertyUtil.getGraduated(ENDPOINT, extensionProperties, String.class);
    }
    return endpoint;
  }

  @JsonProperty(ENDPOINT)
  public OtlpGrpcExporterModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Configure TLS settings for the exporter.
   *
   * <p>If omitted, system default TLS settings are used.
   */
  @JsonProperty(TLS)
  @Nullable
  public GrpcTlsModel getTls() {
    if (tls == null) {
      return ExtensionPropertyUtil.getGraduated(TLS, extensionProperties, GrpcTlsModel.class);
    }
    return tls;
  }

  @JsonProperty(TLS)
  public OtlpGrpcExporterModel withTls(GrpcTlsModel tls) {
    this.tls = tls;
    return this;
  }

  /**
   * Configure headers. Entries have higher priority than entries from .headers_list.
   *
   * <p>If an entry's .value is null, the entry is ignored.
   *
   * <p>If omitted, no headers are added.
   */
  @JsonProperty(HEADERS)
  @Nullable
  public List<NameStringValuePairModel> getHeaders() {
    return headers;
  }

  @JsonProperty(HEADERS)
  public OtlpGrpcExporterModel withHeaders(List<NameStringValuePairModel> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * Configure headers. Entries have lower priority than entries from .headers.
   *
   * <p>The value is a list of comma separated key-value pairs matching the format of
   * OTEL_EXPORTER_OTLP_HEADERS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options
   * for details.
   *
   * <p>If omitted or null, no headers are added.
   */
  @JsonProperty(HEADERS_LIST)
  @Nullable
  public String getHeadersList() {
    if (headersList == null) {
      return ExtensionPropertyUtil.getGraduated(HEADERS_LIST, extensionProperties, String.class);
    }
    return headersList;
  }

  @JsonProperty(HEADERS_LIST)
  public OtlpGrpcExporterModel withHeadersList(String headersList) {
    this.headersList = headersList;
    return this;
  }

  /**
   * Configure compression.
   *
   * <p>Known values include: gzip, none. Implementations may support other compression algorithms.
   *
   * <p>If omitted or null, none is used.
   */
  @JsonProperty(COMPRESSION)
  @Nullable
  public String getCompression() {
    if (compression == null) {
      return ExtensionPropertyUtil.getGraduated(COMPRESSION, extensionProperties, String.class);
    }
    return compression;
  }

  @JsonProperty(COMPRESSION)
  public OtlpGrpcExporterModel withCompression(String compression) {
    this.compression = compression;
    return this;
  }

  /**
   * Configure max time (in milliseconds) to wait for each export.
   *
   * <p>Value must be non-negative. A value of 0 indicates no limit (infinity).
   *
   * <p>If omitted or null, 10000 is used.
   */
  @JsonProperty(TIMEOUT)
  @Nullable
  public Integer getTimeout() {
    if (timeout == null) {
      return ExtensionPropertyUtil.getGraduated(TIMEOUT, extensionProperties, Integer.class);
    }
    return timeout;
  }

  @JsonProperty(TIMEOUT)
  public OtlpGrpcExporterModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public OtlpGrpcExporterModel withExtensionProperty(String name, @Nullable Object value) {
    ExtensionPropertyUtil.handleAnySetter(
        name,
        value,
        extensionProperties,
        Collections.emptyMap(),
        STABLE_PROPERTIES,
        ALLOWS_ADDITIONAL_PROPERTIES);
    return this;
  }

  @Override
  public String toString() {
    return "OtlpGrpcExporterModel{"
        + "endpoint="
        + endpoint
        + ", tls="
        + tls
        + ", headers="
        + headers
        + ", headersList="
        + headersList
        + ", compression="
        + compression
        + ", timeout="
        + timeout
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.getEndpoint() == null) ? 0 : this.getEndpoint().hashCode();
    h *= 1000003;
    h ^= (this.getTls() == null) ? 0 : this.getTls().hashCode();
    h *= 1000003;
    h ^= (this.headers == null) ? 0 : this.headers.hashCode();
    h *= 1000003;
    h ^= (this.getHeadersList() == null) ? 0 : this.getHeadersList().hashCode();
    h *= 1000003;
    h ^= (this.getCompression() == null) ? 0 : this.getCompression().hashCode();
    h *= 1000003;
    h ^= (this.getTimeout() == null) ? 0 : this.getTimeout().hashCode();
    h *= 1000003;
    h ^= (this.getExtensionProperties() == null) ? 0 : this.getExtensionProperties().hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OtlpGrpcExporterModel) {
      OtlpGrpcExporterModel that = (OtlpGrpcExporterModel) o;
      return (this.getEndpoint() == null
              ? that.getEndpoint() == null
              : this.getEndpoint().equals(that.getEndpoint()))
          && (this.getTls() == null ? that.getTls() == null : this.getTls().equals(that.getTls()))
          && (this.headers == null ? that.headers == null : this.headers.equals(that.headers))
          && (this.getHeadersList() == null
              ? that.getHeadersList() == null
              : this.getHeadersList().equals(that.getHeadersList()))
          && (this.getCompression() == null
              ? that.getCompression() == null
              : this.getCompression().equals(that.getCompression()))
          && (this.getTimeout() == null
              ? that.getTimeout() == null
              : this.getTimeout().equals(that.getTimeout()))
          && (this.getExtensionProperties() == null
              ? that.getExtensionProperties() == null
              : this.getExtensionProperties().equals(that.getExtensionProperties()));
    }
    return false;
  }
}
