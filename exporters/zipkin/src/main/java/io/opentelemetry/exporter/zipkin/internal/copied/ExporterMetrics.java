/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public interface ExporterMetrics {

  Recording startRecordingExport(int itemCount);

  /**
   * This class is internal and is hence not for public use. Its APIs are unstable and can change at
   * any time.
   */
  abstract class Recording {

    private boolean alreadyEnded = false;

    protected Recording() {}

    public final void finishSuccessful(Attributes requestAttributes) {
      ensureEndedOnce();
      doFinish(null, requestAttributes);
    }

    public final void finishFailed(String errorType, Attributes requestAttributes) {
      ensureEndedOnce();
      if (errorType == null || errorType.isEmpty()) {
        throw new IllegalArgumentException("The export failed but no failure reason was provided");
      }
      doFinish(errorType, requestAttributes);
    }

    private void ensureEndedOnce() {
      if (alreadyEnded) {
        throw new IllegalStateException("Recording already ended");
      }
      alreadyEnded = true;
    }

    /**
     * Invoked when the export has finished, either successfully or failed.
     *
     * @param errorType null if the export was successful, otherwise a failure reason suitable for
     *     the error.type attribute
     * @param requestAttributes additional attributes to add to request metrics
     */
    protected abstract void doFinish(@Nullable String errorType, Attributes requestAttributes);
  }
}
