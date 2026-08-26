/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.MeterProviderModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link MeterProviderModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class MeterProviderModelAccessor {

  private MeterProviderModelAccessor() {}

  static final String METER_CONFIGURATOR = "meter_configurator/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(METER_CONFIGURATOR, ExperimentalMeterConfiguratorModel.class);
  }

  @Nullable
  public static ExperimentalMeterConfiguratorModel getMeterConfigurator(MeterProviderModel model) {
    return ExtensionPropertyUtil.get(
        METER_CONFIGURATOR,
        model.getExtensionProperties(),
        ExperimentalMeterConfiguratorModel.class);
  }

  public static MeterProviderModel withMeterConfigurator(
      MeterProviderModel model, ExperimentalMeterConfiguratorModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(METER_CONFIGURATOR, value);
    return model;
  }
}
