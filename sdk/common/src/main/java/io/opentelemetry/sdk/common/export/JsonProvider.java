/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A service provider interface (SPI) for providing JSON streaming capabilities backed by different
 * JSON libraries (e.g. Jackson 2, Jackson 3).
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface JsonProvider {

  /** Creates a {@link JsonWriter} that writes to the given {@link OutputStream}. */
  JsonWriter createJsonWriter(OutputStream output) throws IOException;

  /**
   * Creates a {@link JsonWriter} that writes to the given {@link OutputStream}.
   *
   * @param autoCloseTarget if {@code false}, the underlying {@link OutputStream} will not be closed
   *     when the writer is closed
   */
  JsonWriter createJsonWriter(OutputStream output, boolean autoCloseTarget) throws IOException;

  /**
   * Creates a {@link JsonStringWriter} that writes JSON to an in-memory string buffer. Use {@link
   * JsonStringWriter#getAndClear()} to retrieve the result after closing the writer.
   */
  JsonStringWriter createJsonStringWriter() throws IOException;
}
