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

package io.opentelemetry.sdk.logging;

import io.opentelemetry.sdk.logging.data.LogRecord;

/** A LogSink accepts logging records for transmission to an aggregator or log processing system. */
public interface LogSink {
  /**
   * Pass a record to the SDK for transmission to a logging exporter.
   *
   * @param record record to transmit
   */
  void offer(LogRecord record);
}
