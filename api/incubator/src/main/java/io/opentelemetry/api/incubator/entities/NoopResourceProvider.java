/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.entities;

final class NoopResourceProvider implements ResourceProvider {

  @Override
  public Resource getResource() {
    return new NoopResource();
  }
}
