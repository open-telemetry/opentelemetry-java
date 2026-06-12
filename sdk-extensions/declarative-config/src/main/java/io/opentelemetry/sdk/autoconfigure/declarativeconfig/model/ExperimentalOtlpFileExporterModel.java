/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"output_stream"})
@Generated("jsonschema2pojo")
@SuppressWarnings({"NullAway", "rawtypes", "BoxedPrimitiveEquality"})
public class ExperimentalOtlpFileExporterModel {

  /**
   * Configure output stream. Values include stdout, or scheme+destination. For example:
   * file:///path/to/file.jsonl. If omitted or null, stdout is used.
   *
   * <p>(Can be null)
   */
  @Nullable
  @JsonProperty("output_stream")
  @JsonPropertyDescription(
      "Configure output stream. \nValues include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.\nIf omitted or null, stdout is used.\n")
  private String outputStream;

  /**
   * Configure output stream. Values include stdout, or scheme+destination. For example:
   * file:///path/to/file.jsonl. If omitted or null, stdout is used.
   */
  @JsonProperty("output_stream")
  @Nullable
  public String getOutputStream() {
    return outputStream;
  }

  public ExperimentalOtlpFileExporterModel withOutputStream(String outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(ExperimentalOtlpFileExporterModel.class.getName())
        .append('@')
        .append(Integer.toHexString(System.identityHashCode(this)))
        .append('[');
    sb.append("outputStream");
    sb.append('=');
    sb.append(((this.outputStream == null) ? "<null>" : this.outputStream));
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
    result = ((result * 31) + ((this.outputStream == null) ? 0 : this.outputStream.hashCode()));
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if ((other instanceof ExperimentalOtlpFileExporterModel) == false) {
      return false;
    }
    ExperimentalOtlpFileExporterModel rhs = ((ExperimentalOtlpFileExporterModel) other);
    return ((this.outputStream == rhs.outputStream)
        || ((this.outputStream != null) && this.outputStream.equals(rhs.outputStream)));
  }
}
