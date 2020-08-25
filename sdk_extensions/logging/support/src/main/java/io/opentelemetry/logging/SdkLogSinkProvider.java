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

import io.opentelemetry.logging.api.Exporter;
import io.opentelemetry.logging.api.LogRecord;
import io.opentelemetry.logging.api.LogSink;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class SdkLogSinkProvider implements LoggingBatchExporter {
  private final LoggingBatchStrategy batchStrategy;
  private final List<Exporter> exporters;
  private final LogSink logSink = new SdkLogSink();

  private SdkLogSinkProvider(LoggingBatchStrategy batchStrategy, List<Exporter> exporters) {
    this.batchStrategy = batchStrategy != null ? batchStrategy : getDefaultBatchStrategy();
    this.batchStrategy.setBatchHandler(this);
    this.exporters = exporters;
  }

  private static LoggingBatchStrategy getDefaultBatchStrategy() {
    return new SizeOrLatencyBatchStrategy.Builder().build();
  }

  public LogSink get(String instrumentationName, String instrumentationVersion) {
    // Currently there is no differentiation by instrumentation library
    return logSink;
  }

  @Override
  public void handleLogRecordBatch(Collection<LogRecord> batch) {
    for (Exporter e : exporters) {
      e.accept(batch);
    }
  }

  private class SdkLogSink implements LogSink {
    @Override
    public void offer(LogRecord record) {
      batchStrategy.add(record);
    }

    @Override
    public LogRecord.Builder buildRecord() {
      return new LogRecord.Builder();
    }
  }

  public static class Builder {
    @Nullable private LoggingBatchStrategy batchManager = null;
    private final List<Exporter> exporters = new ArrayList<>();

    public Builder withBatchManager(LoggingBatchStrategy manager) {
      batchManager = manager;
      return this;
    }

    public Builder withExporter(Exporter exporter) {
      exporters.add(exporter);
      return this;
    }

    public SdkLogSinkProvider build() {
      return new SdkLogSinkProvider(batchManager, exporters);
    }
  }
}
