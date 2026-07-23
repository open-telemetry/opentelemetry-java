/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"endpoint", "tls", "headers", "headers_list", "compression", "timeout"})
@Generated("io.opentelemetry.gradle.DeclarativeConfigPojoGenerator")
public class OtlpGrpcExporterModel {

  @Nullable private String endpoint;
  @Nullable private GrpcTlsModel tls;
  @Nullable private List<NameStringValuePairModel> headers;
  @Nullable private String headersList;
  @Nullable private String compression;
  @Nullable private Integer timeout;

  /**
   * Configure endpoint.
   *
   * <p>If omitted or null, http://localhost:4317 is used.
   */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  @JsonProperty("endpoint")
  public OtlpGrpcExporterModel withEndpoint(String endpoint) {
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
  public GrpcTlsModel getTls() {
    return tls;
  }

  @JsonProperty("tls")
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
  @JsonProperty("headers")
  @Nullable
  public List<NameStringValuePairModel> getHeaders() {
    return headers;
  }

  @JsonProperty("headers")
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
  @JsonProperty("headers_list")
  @Nullable
  public String getHeadersList() {
    return headersList;
  }

  @JsonProperty("headers_list")
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
  @JsonProperty("compression")
  @Nullable
  public String getCompression() {
    return compression;
  }

  @JsonProperty("compression")
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
  @JsonProperty("timeout")
  @Nullable
  public Integer getTimeout() {
    return timeout;
  }

  @JsonProperty("timeout")
  public OtlpGrpcExporterModel withTimeout(Integer timeout) {
    this.timeout = timeout;
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
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OtlpGrpcExporterModel) {
      OtlpGrpcExporterModel that = (OtlpGrpcExporterModel) o;
      return (this.endpoint == null ? that.endpoint == null : this.endpoint.equals(that.endpoint))
          && (this.tls == null ? that.tls == null : this.tls.equals(that.tls))
          && (this.headers == null ? that.headers == null : this.headers.equals(that.headers))
          && (this.headersList == null
              ? that.headersList == null
              : this.headersList.equals(that.headersList))
          && (this.compression == null
              ? that.compression == null
              : this.compression.equals(that.compression))
          && (this.timeout == null ? that.timeout == null : this.timeout.equals(that.timeout));
    }
    return false;
  }
}
