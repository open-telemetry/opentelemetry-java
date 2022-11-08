/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.aws;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;

/**
 * A {@link ConfigurablePropagatorProvider} which allows enabling the {@link AwsXrayPropagator} with
 * the propagator name {@code xray}.
 *
 * @deprecated Moved to <a
 *     href="https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/aws-xray-propagator">io.opentelemetry.contrib:opentelemetry-aws-xray-propagator</a>.
 */
@Deprecated
public final class AwsConfigurablePropagator implements ConfigurablePropagatorProvider {
  @Override
  public TextMapPropagator getPropagator(ConfigProperties config) {
    return AwsXrayPropagator.getInstance();
  }

  @Override
  public String getName() {
    return "xray";
  }
}
