/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"exporter"})
@Generated("jsonschema2pojo")
public class SimpleLogRecordProcessorModel {

  @Nullable private LogRecordExporterModel exporter;

  /**
   * Configure exporter.
   *
   * <p>Property is required and must be non-null.
   */
  @JsonProperty("exporter")
  @Nullable
  public LogRecordExporterModel getExporter() {
    return exporter;
  }

  @JsonProperty("exporter")
  public SimpleLogRecordProcessorModel withExporter(LogRecordExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  @Override
  public String toString() {
    return "SimpleLogRecordProcessorModel{" + "exporter=" + exporter + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.exporter == null) ? 0 : this.exporter.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SimpleLogRecordProcessorModel) {
      SimpleLogRecordProcessorModel that = (SimpleLogRecordProcessorModel) o;
      return (this.exporter == null ? that.exporter == null : this.exporter.equals(that.exporter));
    }
    return false;
  }
}
