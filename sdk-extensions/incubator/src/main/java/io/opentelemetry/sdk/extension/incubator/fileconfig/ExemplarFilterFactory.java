/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProviderModel;
import io.opentelemetry.sdk.metrics.ExemplarFilter;

final class ExemplarFilterFactory
    implements Factory<MeterProviderModel.ExemplarFilter, ExemplarFilter> {

  private static final ExemplarFilterFactory INSTANCE = new ExemplarFilterFactory();

  private ExemplarFilterFactory() {}

  static ExemplarFilterFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ExemplarFilter create(
      MeterProviderModel.ExemplarFilter model, DeclarativeConfigContext context) {
    switch (model) {
      case ALWAYS_ON:
        return ExemplarFilter.alwaysOn();
      case ALWAYS_OFF:
        return ExemplarFilter.alwaysOff();
      case TRACE_BASED:
        return ExemplarFilter.traceBased();
    }
    throw new DeclarativeConfigException("Unrecognized exemplar filter: " + model);
  }
}
