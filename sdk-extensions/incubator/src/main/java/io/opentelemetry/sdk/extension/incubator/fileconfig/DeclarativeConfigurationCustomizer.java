/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;
import java.util.function.Function;

public interface DeclarativeConfigurationCustomizer {
  void addModelCustomizer(
      Function<OpenTelemetryConfigurationModel, OpenTelemetryConfigurationModel> customizer);
}
