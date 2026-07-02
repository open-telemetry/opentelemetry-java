/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "endpoint",
  "tls",
  "headers",
  "headers_list",
  "compression",
  "timeout",
  "encoding"
})
@Generated("jsonschema2pojo")
public class OtlpHttpExporterModel {

  @JsonProperty("endpoint")
  @Nullable
  private String endpoint;

  @JsonProperty("tls")
  @Nullable
  private HttpTlsModel tls;

  @JsonProperty("headers")
  @Nullable
  private List<NameStringValuePairModel> headers;

  @JsonProperty("headers_list")
  @Nullable
  private String headersList;

  @JsonProperty("compression")
  @Nullable
  private String compression;

  @JsonProperty("timeout")
  @Nullable
  private Integer timeout;

  @JsonProperty("encoding")
  @Nullable
  private OtlpHttpExporterModel.OtlpHttpEncoding encoding;

  /**
   * Configure endpoint, including the signal specific path.
   *
   * <p>If omitted or null, the http://localhost:4318/v1/{signal} (where signal is 'traces', 'logs',
   * or 'metrics') is used.
   */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public OtlpHttpExporterModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Configure TLS settings for the exporter.
   *
   * <p>If omitted, system default TLS settings are used.
   */
  @JsonProperty("tls")
  @Nullable
  public HttpTlsModel getTls() {
    return tls;
  }

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
  @JsonProperty("headers")
  @Nullable
  public List<NameStringValuePairModel> getHeaders() {
    return headers;
  }

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
  @JsonProperty("headers_list")
  @Nullable
  public String getHeadersList() {
    return headersList;
  }

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
  @JsonProperty("compression")
  @Nullable
  public String getCompression() {
    return compression;
  }

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
  @JsonProperty("timeout")
  @Nullable
  public Integer getTimeout() {
    return timeout;
  }

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
  @JsonProperty("encoding")
  @Nullable
  public OtlpHttpExporterModel.OtlpHttpEncoding getEncoding() {
    return encoding;
  }

  public OtlpHttpExporterModel withEncoding(OtlpHttpExporterModel.OtlpHttpEncoding encoding) {
    this.encoding = encoding;
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
          && (this.encoding == null ? that.encoding == null : this.encoding.equals(that.encoding));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
  public enum OtlpHttpEncoding {
    PROTOBUF("protobuf"),
    JSON("json");
    private final String value;
    private static final Map<String, OtlpHttpExporterModel.OtlpHttpEncoding> CONSTANTS =
        new HashMap<String, OtlpHttpExporterModel.OtlpHttpEncoding>();

    static {
      for (OtlpHttpExporterModel.OtlpHttpEncoding c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    OtlpHttpEncoding(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    @JsonValue
    public String value() {
      return this.value;
    }

    @JsonCreator
    public static OtlpHttpExporterModel.OtlpHttpEncoding fromValue(String value) {
      OtlpHttpExporterModel.OtlpHttpEncoding constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
