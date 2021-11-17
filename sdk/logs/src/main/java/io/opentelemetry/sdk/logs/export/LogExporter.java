/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.Collection;

/**
 * An exporter is responsible for taking a collection of {@link LogData}s and transmitting them to
 * their ultimate destination.
 */
public interface LogExporter {

  /**
   * Exports the collections of given {@link LogData}.
   *
   * @param logs the collection of {@link LogData} to be exported
   * @return the result of the export, which is often an asynchronous operation
   */
  CompletableResultCode export(Collection<LogData> logs);

  /**
   * Exports the collection of {@link LogData} that have not yet been exported.
   *
   * @return the result of the flush, which is often an asynchronous operation
   */
  CompletableResultCode shutdown();
}
