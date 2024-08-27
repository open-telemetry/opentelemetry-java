/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import java.io.IOException;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
final class JsonUtil {

  public static final JsonFactory JSON_FACTORY = new JsonFactory();

  static JsonGenerator create(SegmentedStringWriter stringWriter) {
    try {
      return JSON_FACTORY.createGenerator(stringWriter);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create in-memory JsonGenerator, can't happen.", e);
    }
  }

  private JsonUtil() {}
}
