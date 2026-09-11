/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import java.io.IOException;

/**
 * Exposes process context information to external readers using the shared memory mechanism
 * specified in OTEP-4719.
 *
 * @see <a
 *     href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/oteps/profiles/4719-process-ctx.md">OTEP
 *     4719</a>
 */
public interface ProcessContextPublisher extends AutoCloseable {

  /**
   * Called to publish a {@code ProcessContextData}s.
   *
   * @param processContextData the process Resource descriptor and optional additional metadata.
   * @param clock the time source for the publication.
   */
  void publish(ProcessContextData processContextData, Clock clock) throws IOException;
}
