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
@JsonPropertyOrder({"output_stream"})
@Generated("jsonschema2pojo")
public class ExperimentalOtlpFileExporterModel {

  @JsonProperty("output_stream")
  @Nullable
  private String outputStream;

  /**
   * Configure output stream.
   *
   * <p>Values include stdout, or scheme+destination. For example: file:///path/to/file.jsonl.
   *
   * <p>If omitted or null, stdout is used.
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
    return "ExperimentalOtlpFileExporterModel{" + "outputStream=" + outputStream + "}";
  }

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.outputStream == null) ? 0 : this.outputStream.hashCode();
    return h;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ExperimentalOtlpFileExporterModel) {
      ExperimentalOtlpFileExporterModel that = (ExperimentalOtlpFileExporterModel) o;
      return (this.outputStream == null
          ? that.outputStream == null
          : this.outputStream.equals(that.outputStream));
    }
    return false;
  }
}
