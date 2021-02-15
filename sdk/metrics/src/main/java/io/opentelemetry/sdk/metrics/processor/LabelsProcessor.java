/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;

public interface LabelsProcessor {
  /**
   * Called when bind() method is called. Allows to manipulate labels which this instrument is bound
   * to. Particular use case includes enriching lables and/or adding more labels depending on the
   * Context
   *
   * @param ctx context of the operation
   * @param labels immutable labels. When processors are chained output labels of the previous one
   *     is passed as an input to the next one. Last labels returned by a chain of processors are
   *     used for bind() operation.
   * @return labels to be used as an input to the next processor in chain or bind() operation if
   *     this is the last processor
   */
  Labels onLabelsBound(Context ctx, Labels labels);
}
