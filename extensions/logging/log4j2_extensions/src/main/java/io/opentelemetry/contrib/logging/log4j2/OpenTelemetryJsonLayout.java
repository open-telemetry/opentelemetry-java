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

package io.opentelemetry.contrib.logging.log4j2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.spi.StandardLevel;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.Strings;

@Plugin(name = "OpenTelemetryJsonLayout", category = "Core", elementType = "layout")
public class OpenTelemetryJsonLayout extends AbstractStringLayout {
  JsonFactory factory = new JsonFactory();

  protected OpenTelemetryJsonLayout() {
    super(StandardCharsets.UTF_8);
  }

  @Override
  public String toSerializable(LogEvent event) {
    StringBuilderWriter writer = new StringBuilderWriter();
    try {
      JsonGenerator generator = factory.createGenerator(writer);
      generator.writeStartObject();
      writeTimestamp(generator, event.getInstant());

      generator.writeFieldName("name");
      generator.writeString(event.getLoggerName());

      generator.writeFieldName("body");
      generator.writeString(event.getMessage().getFormattedMessage());

      generator.writeFieldName("severitytext");
      generator.writeString(event.getLevel().name());

      generator.writeFieldName("severitynumber");
      generator.writeNumber(decodeSeverity(event.getLevel()));

      if (event.getContextData().containsKey("traceid")) {
        writeRequestCorrelation(generator, event.getContextData());
      }
      generator.writeEndObject();
      generator.close();
      return writer.toString();
    } catch (IOException e) {
      LOGGER.error(e);
      return Strings.EMPTY;
    }
  }

  private static int decodeSeverity(Level level) {
    int intLevel = level.intLevel();
    if (intLevel <= StandardLevel.FATAL.intLevel() && intLevel > 0) {
      return OpenTelemetryLogLevels.FATAL.asInt();
    } else if (intLevel <= StandardLevel.ERROR.intLevel()) {
      return OpenTelemetryLogLevels.ERROR.asInt();
    } else if (intLevel <= StandardLevel.WARN.intLevel()) {
      return OpenTelemetryLogLevels.WARN.asInt();
    } else if (intLevel <= StandardLevel.INFO.intLevel()) {
      return OpenTelemetryLogLevels.INFO.asInt();
    } else if (intLevel <= StandardLevel.DEBUG.intLevel()) {
      return OpenTelemetryLogLevels.DEBUG.asInt();
    } else if (intLevel <= StandardLevel.TRACE.intLevel()) {
      return OpenTelemetryLogLevels.TRACE.asInt();
    } else {
      return OpenTelemetryLogLevels.UNSET.asInt();
    }
  }

  private static void writeRequestCorrelation(
      JsonGenerator generator, ReadOnlyStringMap contextData) throws IOException {
    generator.writeFieldName("traceid");
    generator.writeString(contextData.getValue("traceid").toString());
    generator.writeFieldName("spanid");
    generator.writeString(contextData.getValue("spanid").toString());
    generator.writeFieldName("traceflags");
    generator.writeNumber(contextData.getValue("traceflags").toString());
  }

  private static void writeTimestamp(JsonGenerator generator, Instant instant) throws IOException {
    generator.writeFieldName("timestamp");
    generator.writeStartObject();
    generator.writeFieldName("millis");
    generator.writeNumber(instant.getEpochMillisecond());
    int nanos = instant.getNanoOfMillisecond();
    if (nanos > 0) {
      generator.writeFieldName("nanos");
      generator.writeNumber(instant.getNanoOfMillisecond());
    }
    generator.writeEndObject();
  }

  @PluginFactory
  public static OpenTelemetryJsonLayout build() {
    return new OpenTelemetryJsonLayout();
  }
}
