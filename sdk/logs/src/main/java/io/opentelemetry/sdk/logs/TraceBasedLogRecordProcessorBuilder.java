/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Builder class for {@link TraceBasedLogRecordProcessor}. */
public final class TraceBasedLogRecordProcessorBuilder {

  private final List<LogRecordProcessor> processors = new ArrayList<>();

  TraceBasedLogRecordProcessorBuilder() {}

  /**
   * Adds multiple {@link LogRecordProcessor}s to the list of downstream processors.
   *
   * @param processors the processors to add
   * @return this builder
   */
  public TraceBasedLogRecordProcessorBuilder addProcessors(LogRecordProcessor... processors) {
    requireNonNull(processors, "processors");
    addProcessors(Arrays.asList(processors));
    return this;
  }

  /**
   * Adds multiple {@link LogRecordProcessor}s to the list of downstream processors.
   *
   * @param processors the processors to add
   * @return this builder
   */
  public TraceBasedLogRecordProcessorBuilder addProcessors(
      Iterable<LogRecordProcessor> processors) {

    requireNonNull(processors, "processors");
    for (LogRecordProcessor processor : processors) {
      requireNonNull(processor, "processor");
      this.processors.add(processor);
    }
    return this;
  }

  /**
   * Returns a new {@link TraceBasedLogRecordProcessor} with the configuration of this builder.
   *
   * @return a new {@link TraceBasedLogRecordProcessor}
   * @throws IllegalArgumentException if no processors have been added
   */
  public TraceBasedLogRecordProcessor build() {
    if (processors.isEmpty()) {
      throw new IllegalArgumentException("At least one processor must be added");
    }
    return new TraceBasedLogRecordProcessor(processors);
  }
}
