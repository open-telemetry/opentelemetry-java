/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.authenticator.ExporterAuthenticator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.util.Collections;

public class TestAuthComponentProvider implements ComponentProvider<ExporterAuthenticator> {
  @Override
  public Class<ExporterAuthenticator> getType() {
    return ExporterAuthenticator.class;
  }

  @Override
  public String getName() {
    return "rainy_cloud";
  }

  @Override
  public ExporterAuthenticator create(DeclarativeConfigProperties config) {
    return () -> Collections.singletonMap("auth_provider_key1", "value1");
  }
}
