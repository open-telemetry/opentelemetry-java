/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.Closeable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * An interface that allows different profiling services to export recorded data in their own
 * format.
 */
public interface ProfileExporter extends Closeable {

  /**
   * Called to export sampled {@code ProfileData}s. Note that export operations can be performed
   * simultaneously depending on the type of processor being used.
   *
   * @param profiles the collection of sampled profiles to be exported.
   * @return the result of the export, which is often an asynchronous operation.
   */
  CompletableResultCode export(Collection<ProfileData> profiles);

  /**
   * Exports the collection of sampled {@code ProfileData}s that have not yet been exported. Note
   * that export operations can be performed simultaneously depending on the type of span processor
   * being used.
   *
   * @return the result of the flush, which is often an asynchronous operation.
   */
  CompletableResultCode flush();

  /**
   * Asynchronously terminate operation of the exporter.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes.
   */
  CompletableResultCode shutdown();

  /** Closes this {@link ProfileExporter}, releasing any resources. */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
