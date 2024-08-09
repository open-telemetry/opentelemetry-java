/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.io.OutputStream;
import java.util.StringJoiner;

public class StreamExporterBuilder<T extends Marshaler> {

  @SuppressWarnings("SystemOut")
  private OutputStream outputStream = System.out;

  public StreamExporterBuilder() {}

  public StreamExporterBuilder<T> setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
    return this;
  }

  public StreamExporterBuilder<T> copy() {
    return new StreamExporterBuilder<T>().setOutputStream(outputStream);
  }

  public StreamExporter<T> build() {
    return new StreamExporter<>(outputStream);
  }

  public String toString(boolean includePrefixAndSuffix) {
    StringJoiner joiner =
        includePrefixAndSuffix
            ? new StringJoiner(", ", "StreamExporterBuilder{", "}")
            : new StringJoiner(", ");
    joiner.add("outputStream=" + outputStream);
    return joiner.toString();
  }
}
