/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.IOException;

/**
 * A {@link JsonWriter} that accumulates output into a {@link String}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface JsonStringWriter {

  /** Returns the underlying {@link JsonWriter}. */
  JsonWriter writer();

  /** Returns the accumulated JSON string. Call after closing the writer. */
  String getAndClear() throws IOException;
}
