/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OpenTelemetryConfigurationModel;
import io.opentelemetry.sdk.resources.Resource;

/** The result of {@link DeclarativeConfiguration#create(OpenTelemetryConfigurationModel)}. */
public final class DeclarativeConfigResult {

  private final OpenTelemetrySdk sdk;
  private final Resource resource;

  DeclarativeConfigResult(OpenTelemetrySdk sdk, Resource resource) {
    this.sdk = sdk;
    this.resource = resource;
  }

  public OpenTelemetrySdk getSdk() {
    return sdk;
  }

  public Resource getResource() {
    return resource;
  }
}
