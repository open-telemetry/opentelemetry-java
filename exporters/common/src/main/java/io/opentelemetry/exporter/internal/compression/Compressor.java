/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An abstraction for compressing messages. Implementation MUST be thread safe as the same instance
 * is expected to be used many times and concurrently. Instances are usually singletons.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
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
