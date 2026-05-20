/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"endpoint", "tls", "headers", "headers_list", "compression", "timeout"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class OtlpGrpcExporterModel {

  /**
   * Configure endpoint. If omitted or null, http://localhost:4317 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("endpoint")
  @JsonPropertyDescription(
      "Configure endpoint.\nIf omitted or null, http://localhost:4317 is used.\n")
  private String endpoint;

  /** (Can be null) */
  @Nullable
  @JsonProperty("tls")
  private GrpcTlsModel tls;

  /**
   * Configure headers. Entries have higher priority than entries from .headers_list. If an entry's
   * .value is null, the entry is ignored. If omitted, no headers are added.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("headers")
  @JsonPropertyDescription(
      "Configure headers. Entries have higher priority than entries from .headers_list.\nIf an entry's .value is null, the entry is ignored.\nIf omitted, no headers are added.\n")
  private List<NameStringValuePairModel> headers;

  /**
   * Configure headers. Entries have lower priority than entries from .headers. The value is a list
   * of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options
   * for details. If omitted or null, no headers are added.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("headers_list")
  @JsonPropertyDescription(
      "Configure headers. Entries have lower priority than entries from .headers.\nThe value is a list of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options for details.\nIf omitted or null, no headers are added.\n")
  private String headersList;

  /**
   * Configure compression. Known values include: gzip, none. Implementations may support other
   * compression algorithms. If omitted or null, none is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("compression")
  @JsonPropertyDescription(
      "Configure compression.\nKnown values include: gzip, none. Implementations may support other compression algorithms.\nIf omitted or null, none is used.\n")
  private String compression;

  /**
   * Configure max time (in milliseconds) to wait for each export. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 10000 is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("timeout")
  @JsonPropertyDescription(
      "Configure max time (in milliseconds) to wait for each export.\nValue must be non-negative. A value of 0 indicates no limit (infinity).\nIf omitted or null, 10000 is used.\n")
  private Integer timeout;

  /** Configure endpoint. If omitted or null, http://localhost:4317 is used. */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public OtlpGrpcExporterModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  @JsonProperty("tls")
  @Nullable
  public GrpcTlsModel getTls() {
    return tls;
  }

  public OtlpGrpcExporterModel withTls(GrpcTlsModel tls) {
    this.tls = tls;
    return this;
  }

  /**
   * Configure headers. Entries have higher priority than entries from .headers_list. If an entry's
   * .value is null, the entry is ignored. If omitted, no headers are added.
   */
  @JsonProperty("headers")
  @Nullable
  public List<NameStringValuePairModel> getHeaders() {
    return headers;
  }

  public OtlpGrpcExporterModel withHeaders(List<NameStringValuePairModel> headers) {
    this.headers = headers;
    return this;
  }

  /**
   * Configure headers. Entries have lower priority than entries from .headers. The value is a list
   * of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options
   * for details. If omitted or null, no headers are added.
   */
  @JsonProperty("headers_list")
  @Nullable
  public String getHeadersList() {
    return headersList;
  }

  public OtlpGrpcExporterModel withHeadersList(String headersList) {
    this.headersList = headersList;
    return this;
  }

  /**
   * Configure compression. Known values include: gzip, none. Implementations may support other
   * compression algorithms. If omitted or null, none is used.
   */
  @JsonProperty("compression")
  @Nullable
  public String getCompression() {
    return compression;
  }

  public OtlpGrpcExporterModel withCompression(String compression) {
    this.compression = compression;
    return this;
  }

  /**
   * Configure max time (in milliseconds) to wait for each export. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 10000 is used.
   */
  @JsonProperty("timeout")
  @Nullable
  public Integer getTimeout() {
    return timeout;
  }

  public OtlpGrpcExporterModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(OtlpGrpcExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("endpoint");
    sb.append('=');
    sb.append(((this.endpoint == null) ? "<null>" : this.endpoint));
    sb.append(',');
    sb.append("tls");
    sb.append('=');
    sb.append(((this.tls == null) ? "<null>" : this.tls));
    sb.append(',');
    sb.append("headers");
    sb.append('=');
    sb.append(((this.headers == null) ? "<null>" : this.headers));
    sb.append(',');
    sb.append("headersList");
    sb.append('=');
    sb.append(((this.headersList == null) ? "<null>" : this.headersList));
    sb.append(',');
    sb.append("compression");
    sb.append('=');
    sb.append(((this.compression == null) ? "<null>" : this.compression));
    sb.append(',');
    sb.append("timeout");
    sb.append('=');
    sb.append(((this.timeout == null) ? "<null>" : this.timeout));
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
    result = ((result * 31) + ((this.headersList == null) ? 0 : this.headersList.hashCode()));
    result = ((result * 31) + ((this.headers == null) ? 0 : this.headers.hashCode()));
    result = ((result * 31) + ((this.endpoint == null) ? 0 : this.endpoint.hashCode()));
    result = ((result * 31) + ((this.tls == null) ? 0 : this.tls.hashCode()));
    result = ((result * 31) + ((this.compression == null) ? 0 : this.compression.hashCode()));
    result = ((result * 31) + ((this.timeout == null) ? 0 : this.timeout.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof OtlpGrpcExporterModel) == false) {
      return false;
    }
    OtlpGrpcExporterModel rhs = ((OtlpGrpcExporterModel) other);
    return (((((((this.headersList == rhs.headersList)
                            || ((this.headersList != null)
                                && this.headersList.equals(rhs.headersList)))
                        && ((this.headers == rhs.headers)
                            || ((this.headers != null) && this.headers.equals(rhs.headers))))
                    && ((this.endpoint == rhs.endpoint)
                        || ((this.endpoint != null) && this.endpoint.equals(rhs.endpoint))))
                && ((this.tls == rhs.tls) || ((this.tls != null) && this.tls.equals(rhs.tls))))
            && ((this.compression == rhs.compression)
                || ((this.compression != null) && this.compression.equals(rhs.compression))))
        && ((this.timeout == rhs.timeout)
            || ((this.timeout != null) && this.timeout.equals(rhs.timeout))));
  }
}
