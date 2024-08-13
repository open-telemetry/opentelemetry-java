/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import java.io.Closeable;
import java.util.List;

interface Factory<ModelT, ResultT> {

  /**
   * Interpret the model and create {@link ResultT} with corresponding configuration.
   *
   * @param model the configuration model
   * @param spiHelper the service loader helper
   * @param closeables mutable list of closeables created
   * @return the {@link ResultT}
   */
  ResultT create(ModelT model, SpiHelper spiHelper, List<Closeable> closeables);
}
