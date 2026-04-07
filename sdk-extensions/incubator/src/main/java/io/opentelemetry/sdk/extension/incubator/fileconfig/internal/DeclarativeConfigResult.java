/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import io.opentelemetry.sdk.internal.ExtendedOpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;

/**
 * The result of {@link io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration#create}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DeclarativeConfigResult {

  private final ExtendedOpenTelemetrySdk sdk;
  private final Resource resource;

  public DeclarativeConfigResult(ExtendedOpenTelemetrySdk sdk, Resource resource) {
    this.sdk = sdk;
    this.resource = resource;
  }

  public ExtendedOpenTelemetrySdk getSdk() {
    return sdk;
  }

  public Resource getResource() {
    return resource;
  }
}
