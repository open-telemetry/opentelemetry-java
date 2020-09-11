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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.logging.data.LogRecord;
import java.util.ArrayList;
import java.util.List;

public class LogSinkSdkProvider {
  private final LogSink logSink = new SdkLogSink();
  private final List<LogProcessor> processors = new ArrayList<>();

  private LogSinkSdkProvider() {}

  public LogSink get(String instrumentationName, String instrumentationVersion) {
    // Currently there is no differentiation by instrumentation library
    return logSink;
  }

  public void addLogProcessor(LogProcessor processor) {
    processors.add(Utils.checkNotNull(processor, "Processor can not be null"));
  }

  /** Flushes all attached processors. */
  public void forceFlush() {
    for (LogProcessor processor : processors) {
      processor.forceFlush();
    }
  }

  private class SdkLogSink implements LogSink {
    @Override
    public void offer(LogRecord record) {
      for (LogProcessor processor : processors) {
        processor.addLogRecord(record);
      }
    }

    @Override
    public LogRecord.Builder buildRecord() {
      return new LogRecord.Builder();
    }
  }

  public static class Builder {

    public LogSinkSdkProvider build() {
      return new LogSinkSdkProvider();
    }
  }
}
