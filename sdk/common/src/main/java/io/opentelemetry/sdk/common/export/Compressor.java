/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A service provider interface (SPI) for compression. Implementation MUST be thread safe as the
 * same instance is expected to be used many times and concurrently.
 *
 * @since 1.59.0
 */
@ThreadSafe
public interface Compressor {

  /**
   * The name of the compressor encoding.
   *
   * <p>Used to identify the compressor during configuration and to populate the {@code
   * Content-Encoding} header.
   */
  String getEncoding();

  /** Wrap the {@code outputStream} with a compressing output stream. */
  OutputStream compress(OutputStream outputStream) throws IOException;
}
