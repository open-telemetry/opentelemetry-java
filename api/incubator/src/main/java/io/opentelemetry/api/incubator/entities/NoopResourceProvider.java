/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

final class NoopResourceProvider implements ResourceProvider {

  static final ResourceProvider INSTANCE = new NoopResourceProvider();

  @Override
  public Resource getResource() {
    return NoopResource.INSTANCE;
  }
}
