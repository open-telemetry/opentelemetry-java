/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import javax.annotation.Nullable;

/** File configuration implementation for {@link ConfigProvider}. */
public final class FileConfigProvider implements ConfigProvider {

  @Nullable private final StructuredConfigProperties instrumentationConfig;

  private FileConfigProvider(OpenTelemetryConfiguration model) {
    StructuredConfigProperties configProperties = FileConfiguration.toConfigProperties(model);
    this.instrumentationConfig = configProperties.getStructured("instrumentation");
  }

  /**
   * Create a {@link FileConfigProvider} from the {@code model}.
   *
   * @param model the configuration model
   * @return the {@link FileConfigProvider}
   */
  public static FileConfigProvider create(OpenTelemetryConfiguration model) {
    return new FileConfigProvider(model);
  }

  @Nullable
  @Override
  public StructuredConfigProperties getInstrumentationConfig() {
    return instrumentationConfig;
  }
}
