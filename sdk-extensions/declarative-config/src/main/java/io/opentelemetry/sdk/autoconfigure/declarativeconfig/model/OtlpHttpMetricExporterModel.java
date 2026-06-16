/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
  "encoding",
  "temporality_preference",
  "default_histogram_aggregation"
})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class OtlpHttpMetricExporterModel {

  /**
   * Configure endpoint. If omitted or null, http://localhost:4318/v1/metrics is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("endpoint")
  @JsonPropertyDescription(
      "Configure endpoint.\nIf omitted or null, http://localhost:4318/v1/metrics is used.\n")
  private String endpoint;

  /** (Can be null) */
  @Nullable
  @JsonProperty("tls")
  private HttpTlsModel tls;

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

  /** (Can be null) */
  @Nullable
  @JsonProperty("encoding")
  private OtlpHttpExporterModel.OtlpHttpEncoding encoding;

  /** (Can be null) */
  @Nullable
  @JsonProperty("temporality_preference")
  private OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference;

  /** (Can be null) */
  @Nullable
  @JsonProperty("default_histogram_aggregation")
  private OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      defaultHistogramAggregation;

  /** Configure endpoint. If omitted or null, http://localhost:4318/v1/metrics is used. */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public OtlpHttpMetricExporterModel withEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  @JsonProperty("tls")
  @Nullable
  public HttpTlsModel getTls() {
    return tls;
  }

  public OtlpHttpMetricExporterModel withTls(HttpTlsModel tls) {
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

  public OtlpHttpMetricExporterModel withHeaders(List<NameStringValuePairModel> headers) {
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

  public OtlpHttpMetricExporterModel withHeadersList(String headersList) {
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

  public OtlpHttpMetricExporterModel withCompression(String compression) {
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

  public OtlpHttpMetricExporterModel withTimeout(Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  @JsonProperty("encoding")
  @Nullable
  public OtlpHttpExporterModel.OtlpHttpEncoding getEncoding() {
    return encoding;
  }

  public OtlpHttpMetricExporterModel withEncoding(OtlpHttpExporterModel.OtlpHttpEncoding encoding) {
    this.encoding = encoding;
    return this;
  }

  @JsonProperty("temporality_preference")
  @Nullable
  public OtlpHttpMetricExporterModel.ExporterTemporalityPreference getTemporalityPreference() {
    return temporalityPreference;
  }

  public OtlpHttpMetricExporterModel withTemporalityPreference(
      OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference) {
    this.temporalityPreference = temporalityPreference;
    return this;
  }

  @JsonProperty("default_histogram_aggregation")
  @Nullable
  public OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      getDefaultHistogramAggregation() {
    return defaultHistogramAggregation;
  }

  public OtlpHttpMetricExporterModel withDefaultHistogramAggregation(
      OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation defaultHistogramAggregation) {
    this.defaultHistogramAggregation = defaultHistogramAggregation;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(OtlpHttpMetricExporterModel.class.getName())
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
    sb.append("encoding");
    sb.append('=');
    sb.append(((this.encoding == null) ? "<null>" : this.encoding));
    sb.append(',');
    sb.append("temporalityPreference");
    sb.append('=');
    sb.append(((this.temporalityPreference == null) ? "<null>" : this.temporalityPreference));
    sb.append(',');
    sb.append("defaultHistogramAggregation");
    sb.append('=');
    sb.append(
        ((this.defaultHistogramAggregation == null) ? "<null>" : this.defaultHistogramAggregation));
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
    result = ((result * 31) + ((this.headers == null) ? 0 : this.headers.hashCode()));
    result = ((result * 31) + ((this.endpoint == null) ? 0 : this.endpoint.hashCode()));
    result = ((result * 31) + ((this.headersList == null) ? 0 : this.headersList.hashCode()));
    result = ((result * 31) + ((this.tls == null) ? 0 : this.tls.hashCode()));
    result = ((result * 31) + ((this.compression == null) ? 0 : this.compression.hashCode()));
    result = ((result * 31) + ((this.encoding == null) ? 0 : this.encoding.hashCode()));
    result =
        ((result * 31)
            + ((this.temporalityPreference == null) ? 0 : this.temporalityPreference.hashCode()));
    result = ((result * 31) + ((this.timeout == null) ? 0 : this.timeout.hashCode()));
    result =
        ((result * 31)
            + ((this.defaultHistogramAggregation == null)
                ? 0
                : this.defaultHistogramAggregation.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof OtlpHttpMetricExporterModel) == false) {
      return false;
    }
    OtlpHttpMetricExporterModel rhs = ((OtlpHttpMetricExporterModel) other);
    return ((((((((((this.headers == rhs.headers)
                                        || ((this.headers != null)
                                            && this.headers.equals(rhs.headers)))
                                    && ((this.endpoint == rhs.endpoint)
                                        || ((this.endpoint != null)
                                            && this.endpoint.equals(rhs.endpoint))))
                                && ((this.headersList == rhs.headersList)
                                    || ((this.headersList != null)
                                        && this.headersList.equals(rhs.headersList))))
                            && ((this.tls == rhs.tls)
                                || ((this.tls != null) && this.tls.equals(rhs.tls))))
                        && ((this.compression == rhs.compression)
                            || ((this.compression != null)
                                && this.compression.equals(rhs.compression))))
                    && ((this.encoding == rhs.encoding)
                        || ((this.encoding != null) && this.encoding.equals(rhs.encoding))))
                && ((this.temporalityPreference == rhs.temporalityPreference)
                    || ((this.temporalityPreference != null)
                        && this.temporalityPreference.equals(rhs.temporalityPreference))))
            && ((this.timeout == rhs.timeout)
                || ((this.timeout != null) && this.timeout.equals(rhs.timeout))))
        && ((this.defaultHistogramAggregation == rhs.defaultHistogramAggregation)
            || ((this.defaultHistogramAggregation != null)
                && this.defaultHistogramAggregation.equals(rhs.defaultHistogramAggregation))));
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
  public enum ExporterDefaultHistogramAggregation {
    EXPLICIT_BUCKET_HISTOGRAM("explicit_bucket_histogram"),
    BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM("base2_exponential_bucket_histogram");
    private final String value;
    private static final Map<
            String, OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation>
        CONSTANTS =
            new HashMap<String, OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation>();

    static {
      for (OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ExporterDefaultHistogramAggregation(String value) {
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
    public static OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation fromValue(
        String value) {
      OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation constant =
          CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }

  @Generated("jsonschema2pojo")
  @SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
  public enum ExporterTemporalityPreference {
    CUMULATIVE("cumulative"),
    DELTA("delta"),
    LOW_MEMORY("low_memory");
    private final String value;
    private static final Map<String, OtlpHttpMetricExporterModel.ExporterTemporalityPreference>
        CONSTANTS =
            new HashMap<String, OtlpHttpMetricExporterModel.ExporterTemporalityPreference>();

    static {
      for (OtlpHttpMetricExporterModel.ExporterTemporalityPreference c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    ExporterTemporalityPreference(String value) {
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
    public static OtlpHttpMetricExporterModel.ExporterTemporalityPreference fromValue(
        String value) {
      OtlpHttpMetricExporterModel.ExporterTemporalityPreference constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }
  }
}
