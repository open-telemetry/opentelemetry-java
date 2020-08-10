/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.logging;

import io.opentelemetry.logging.api.LogRecord;
import java.util.List;

/**
 * A LoggingBatchStrategy encodes the logic for how to batch and aggregate records for transmission.
 * {@link SizeOrLatencyBatchStrategy}
 */
public interface LoggingBatchStrategy {
  /**
   * Add a LogRecord to the existing batch.
   *
   * @param record record to enqueue
   */
  void add(LogRecord record);

  /** Explicitly flush the batch. */
  void flush();

  /**
   * This sets the handler for the batch. {@link LoggingBatchExporter#handleLogRecordBatch(List)}
   * will be called whenever this strategy's constraints are fulfilled.
   *
   * @param handler Exporter to invoke when the batch is full
   */
  void setBatchHandler(LoggingBatchExporter handler);
}
