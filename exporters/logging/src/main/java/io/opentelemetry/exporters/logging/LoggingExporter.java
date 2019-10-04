/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.logging;

import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Span Exporter that logs every span using SLF4J. */
public class LoggingExporter implements SpanExporter {
  private static final Logger logger = LoggerFactory.getLogger(LoggingExporter.class);

  @Override
  public ResultCode export(List<SpanData> spans) {
    for (SpanData span : spans) {
      logger.info("span: %s" + span);
    }
    return ResultCode.SUCCESS;
  }

  @Override
  public void shutdown() {}
}
