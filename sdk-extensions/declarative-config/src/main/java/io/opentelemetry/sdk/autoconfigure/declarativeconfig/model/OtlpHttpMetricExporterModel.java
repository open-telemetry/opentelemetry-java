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
public class OtlpHttpMetricExporterModel {

  /** Configure endpoint. If omitted or null, http://localhost:4318/v1/metrics is used. */
  @JsonProperty("endpoint")
  @JsonPropertyDescription(
      "Configure endpoint.\nIf omitted or null, http://localhost:4318/v1/metrics is used.\n")
  @Nullable
  private String endpoint;

  @JsonProperty("tls")
  @Nullable
  private HttpTlsModel tls;

  /**
   * Configure headers. Entries have higher priority than entries from .headers_list. If an entry's
   * .value is null, the entry is ignored. If omitted, no headers are added.
   */
  @JsonProperty("headers")
  @JsonPropertyDescription(
      "Configure headers. Entries have higher priority than entries from .headers_list.\nIf an entry's .value is null, the entry is ignored.\nIf omitted, no headers are added.\n")
  @Nullable
  private List<NameStringValuePairModel> headers;

  /**
   * Configure headers. Entries have lower priority than entries from .headers. The value is a list
   * of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See
   * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options
   * for details. If omitted or null, no headers are added.
   */
  @JsonProperty("headers_list")
  @JsonPropertyDescription(
      "Configure headers. Entries have lower priority than entries from .headers.\nThe value is a list of comma separated key-value pairs matching the format of OTEL_EXPORTER_OTLP_HEADERS. See https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/protocol/exporter.md#configuration-options for details.\nIf omitted or null, no headers are added.\n")
  @Nullable
  private String headersList;

  /**
   * Configure compression. Known values include: gzip, none. Implementations may support other
   * compression algorithms. If omitted or null, none is used.
   */
  @JsonProperty("compression")
  @JsonPropertyDescription(
      "Configure compression.\nKnown values include: gzip, none. Implementations may support other compression algorithms.\nIf omitted or null, none is used.\n")
  @Nullable
  private String compression;

  /**
   * Configure max time (in milliseconds) to wait for each export. Value must be non-negative. A
   * value of 0 indicates no limit (infinity). If omitted or null, 10000 is used.
   */
  @JsonProperty("timeout")
  @JsonPropertyDescription(
      "Configure max time (in milliseconds) to wait for each export.\nValue must be non-negative. A value of 0 indicates no limit (infinity).\nIf omitted or null, 10000 is used.\n")
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
