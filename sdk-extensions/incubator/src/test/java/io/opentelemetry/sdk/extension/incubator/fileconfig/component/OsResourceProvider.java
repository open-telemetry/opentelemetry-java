/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;

// TODO(jack-berg): This allows DeclarativeConfigurationCreateTest to pass with kitchen-sink.yaml
// example. Delete after resource providers from opentelemetry-java-instrumentation are renamed to
// reflect declarative config naming
public class OsResourceProvider implements ComponentProvider {
  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "os";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return Resource.empty();
  }
}
