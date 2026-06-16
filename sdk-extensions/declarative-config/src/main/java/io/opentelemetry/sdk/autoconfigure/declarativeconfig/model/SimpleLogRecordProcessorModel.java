/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"exporter"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class SimpleLogRecordProcessorModel {

  /** (Required) */
  @JsonProperty("exporter")
  @Nonnull
  private LogRecordExporterModel exporter;

  /** (Required) */
  @JsonProperty("exporter")
  @Nullable
  public LogRecordExporterModel getExporter() {
    return exporter;
  }

  public SimpleLogRecordProcessorModel withExporter(LogRecordExporterModel exporter) {
    this.exporter = exporter;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(SimpleLogRecordProcessorModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("exporter");
    sb.append('=');
    sb.append(((this.exporter == null) ? "<null>" : this.exporter));
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
    result = ((result * 31) + ((this.exporter == null) ? 0 : this.exporter.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof SimpleLogRecordProcessorModel) == false) {
      return false;
    }
    SimpleLogRecordProcessorModel rhs = ((SimpleLogRecordProcessorModel) other);
    return ((this.exporter == rhs.exporter)
        || ((this.exporter != null) && this.exporter.equals(rhs.exporter)));
  }
}
