/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Propagator;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class PropagatorFactory implements Factory<Propagator, ContextPropagators> {

  private static final PropagatorFactory INSTANCE = new PropagatorFactory();

  private PropagatorFactory() {}

  static PropagatorFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ContextPropagators create(
      @Nullable Propagator model, SpiHelper spiHelper, List<Closeable> closeables) {
    List<String> compositeModel = null;
    if (model != null) {
      compositeModel = model.getComposite();
    }
    TextMapPropagator textMapPropagator =
        TextMapPropagatorFactory.getInstance().create(compositeModel, spiHelper, closeables);
    return ContextPropagators.create(textMapPropagator);
  }
}
