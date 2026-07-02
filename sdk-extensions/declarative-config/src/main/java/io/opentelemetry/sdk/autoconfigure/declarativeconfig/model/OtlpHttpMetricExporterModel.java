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
  "encoding",
  "temporality_preference",
  "default_histogram_aggregation"
})
@Generated("jsonschema2pojo")
public class OtlpHttpMetricExporterModel {

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

  @JsonProperty("temporality_preference")
  @Nullable
  private OtlpHttpMetricExporterModel.ExporterTemporalityPreference temporalityPreference;

  @JsonProperty("default_histogram_aggregation")
  @Nullable
  private OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
      defaultHistogramAggregation;

  /**
   * Configure endpoint.
   *
   * <p>If omitted or null, http://localhost:4318/v1/metrics is used.
   */
  @JsonProperty("endpoint")
  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public OtlpHttpMetricExporterModel withEndpoint(String endpoint) {
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

  public OtlpHttpMetricExporterModel withTls(HttpTlsModel tls) {
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

  public OtlpHttpMetricExporterModel withHeaders(List<NameStringValuePairModel> headers) {
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

  public OtlpHttpMetricExporterModel withHeadersList(String headersList) {
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

  public OtlpHttpMetricExporterModel withCompression(String compression) {
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

  public OtlpHttpMetricExporterModel withTimeout(Integer timeout) {
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

  public OtlpHttpMetricExporterModel withEncoding(OtlpHttpExporterModel.OtlpHttpEncoding encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * Configure temporality preference.
   *
   * <p>Values include:
   *
   * <p>* cumulative: Use cumulative aggregation temporality for all instrument types.
   *
   * <p>* delta: Use delta aggregation for all instrument types except up down counter and
   * asynchronous up down counter.
   *
   * <p>* low_memory: Use delta aggregation temporality for counter and histogram instrument types.
   * Use cumulative aggregation temporality for all other instrument types.
   *
   * <p>If omitted, cumulative is used.
   */
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

  /**
   * Configure default histogram aggregation.
   *
   * <p>Values include:
   *
   * <p>* base2_exponential_bucket_histogram: Use base2 exponential histogram as the default
   * aggregation for histogram instruments.
   *
   * <p>* explicit_bucket_histogram: Use explicit bucket histogram as the default aggregation for
   * histogram instruments.
   *
   * <p>If omitted, explicit_bucket_histogram is used.
   */
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
    return "OtlpHttpMetricExporterModel{"
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
        + ", temporalityPreference="
        + temporalityPreference
        + ", defaultHistogramAggregation="
        + defaultHistogramAggregation
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
    h ^= (this.temporalityPreference == null) ? 0 : this.temporalityPreference.hashCode();
    h *= 1000003;
    h ^=
        (this.defaultHistogramAggregation == null)
            ? 0
            : this.defaultHistogramAggregation.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof OtlpHttpMetricExporterModel) {
      OtlpHttpMetricExporterModel that = (OtlpHttpMetricExporterModel) o;
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
          && (this.temporalityPreference == null
              ? that.temporalityPreference == null
              : this.temporalityPreference.equals(that.temporalityPreference))
          && (this.defaultHistogramAggregation == null
              ? that.defaultHistogramAggregation == null
              : this.defaultHistogramAggregation.equals(that.defaultHistogramAggregation));
    }
    return false;
  }

  @Generated("jsonschema2pojo")
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
