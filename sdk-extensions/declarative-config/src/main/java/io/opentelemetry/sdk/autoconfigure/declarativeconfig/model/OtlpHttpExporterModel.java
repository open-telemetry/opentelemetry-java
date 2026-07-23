/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.COMPRESSION;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.ENCODING;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.ENDPOINT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.HEADERS;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.HEADERS_LIST;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.TIMEOUT;
import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel.TLS;

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
@JsonPropertyOrder({ENDPOINT, TLS, HEADERS, HEADERS_LIST, COMPRESSION, TIMEOUT, ENCODING})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class OtlpHttpExporterModel {

  static final String ENDPOINT = "endpoint";
  static final String TLS = "tls";
  static final String HEADERS = "headers";
  static final String HEADERS_LIST = "headers_list";
  static final String COMPRESSION = "compression";
  static final String TIMEOUT = "timeout";
  static final String ENCODING = "encoding";

  private static final Map<String, Class<?>> STABLE_PROPERTIES;

  static {
    STABLE_PROPERTIES = new HashMap<>();
    STABLE_PROPERTIES.put(ENDPOINT, String.class);
    STABLE_PROPERTIES.put(TLS, HttpTlsModel.class);
    STABLE_PROPERTIES.put(HEADERS_LIST, String.class);
    STABLE_PROPERTIES.put(COMPRESSION, String.class);
    STABLE_PROPERTIES.put(TIMEOUT, Integer.class);
    STABLE_PROPERTIES.put(ENCODING, OtlpHttpEncodingModel.class);
  }

  private static final boolean ALLOWS_ADDITIONAL_PROPERTIES = false;

  @Nullable private String endpoint;
  @Nullable private HttpTlsModel tls;
  @Nullable private List<NameStringValuePairModel> headers;
  @Nullable private String headersList;
  @Nullable private String compression;
  @Nullable private Integer timeout;
  @Nullable private OtlpHttpEncodingModel encoding;
  private Map<String, Object> extensionProperties = new LinkedHashMap<String, Object>();

  /**
   * Configure endpoint, including the signal specific path.
   *
   * <p>If omitted or null, the http://localhost:4318/v1/{signal} (where signal is 'traces', 'logs',
   * or 'metrics') is used.
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
  public OtlpHttpExporterModel withEndpoint(String endpoint) {
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
  public HttpTlsModel getTls() {
    if (tls == null) {
      return ExtensionPropertyUtil.getGraduated(TLS, extensionProperties, HttpTlsModel.class);
    }
    return tls;
  }

  @JsonProperty(TLS)
  public OtlpHttpExporterModel withTls(HttpTlsModel tls) {
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
  public OtlpHttpExporterModel withHeaders(List<NameStringValuePairModel> headers) {
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
  public OtlpHttpExporterModel withHeadersList(String headersList) {
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
  public OtlpHttpExporterModel withCompression(String compression) {
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
  public OtlpHttpExporterModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Configure the encoding used for messages.
   *
   * <p>Implementations may not support json.
   *
   * <p>Values include:
   *
   * <p>* json: Protobuf JSON encoding.
   *
   * <p>* protobuf: Protobuf binary encoding.
   *
   * <p>If omitted, protobuf is used.
   */
  @JsonProperty(ENCODING)
  @Nullable
  public OtlpHttpEncodingModel getEncoding() {
    if (encoding == null) {
      return ExtensionPropertyUtil.getGraduated(
          ENCODING, extensionProperties, OtlpHttpEncodingModel.class);
    }
    return encoding;
  }

  @JsonProperty(ENCODING)
  public OtlpHttpExporterModel withEncoding(OtlpHttpEncodingModel encoding) {
    this.encoding = encoding;
    return this;
  }

  @JsonAnyGetter
  public Map<String, Object> getExtensionProperties() {
    return ExtensionPropertyUtil.filterSerializable(extensionProperties, STABLE_PROPERTIES);
  }

  @JsonAnySetter
  public OtlpHttpExporterModel withExtensionProperty(String name, @Nullable Object value) {
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
    return "OtlpHttpExporterModel{"
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
        + ", encoding="
        + encoding
        + ", extensionProperties="
        + extensionProperties
        + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.endpoint == null) ? 0 : this.endpoint.hashCode();
    h *= 1000003;
    h ^= (this.tls == null) ? 0 : this.tls.hashCode();
    h *= 1000003;
    h ^= (this.headers == null) ? 0 : this.headers.hashCode();
    h *= 1000003;
    h ^= (this.headersList == null) ? 0 : this.headersList.hashCode();
    h *= 1000003;
    h ^= (this.compression == null) ? 0 : this.compression.hashCode();
    h *= 1000003;
    h ^= (this.timeout == null) ? 0 : this.timeout.hashCode();
    h *= 1000003;
    h ^= (this.encoding == null) ? 0 : this.encoding.hashCode();
    h *= 1000003;
    h ^= (this.extensionProperties == null) ? 0 : this.extensionProperties.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OtlpHttpExporterModel) {
      OtlpHttpExporterModel that = (OtlpHttpExporterModel) o;
      return (this.endpoint == null ? that.endpoint == null : this.endpoint.equals(that.endpoint))
          && (this.tls == null ? that.tls == null : this.tls.equals(that.tls))
          && (this.headers == null ? that.headers == null : this.headers.equals(that.headers))
          && (this.headersList == null
              ? that.headersList == null
              : this.headersList.equals(that.headersList))
          && (this.compression == null
              ? that.compression == null
              : this.compression.equals(that.compression))
          && (this.timeout == null ? that.timeout == null : this.timeout.equals(that.timeout))
          && (this.encoding == null ? that.encoding == null : this.encoding.equals(that.encoding))
          && (this.extensionProperties == null
              ? that.extensionProperties == null
              : this.extensionProperties.equals(that.extensionProperties));
    }
    return false;
  }
}
