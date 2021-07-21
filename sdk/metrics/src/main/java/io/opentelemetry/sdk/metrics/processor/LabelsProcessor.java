/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.processor;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;

/**
 * Labels processor is an abstraction to manipulate instrument labels during metrics capture
 * process.
 */
public interface LabelsProcessor {
  /**
   * Called when bound synchronous instrument is created or metrics are recorded for non-bound
   * synchronous instrument. Allows to manipulate labels which this instrument is bound to in case
   * of binding operation or labels used for recording values in case of non-bound synchronous
   * instrument. Particular use case includes enriching labels and/or adding more labels depending
   * on the Context
   *
   * <p>Please note, this is an experimental API. In case of bound instruments, it will be only
   * invoked upon instrument binding and not when measurements are recorded.
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
