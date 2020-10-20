/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentracing.Scope;

final class ScopeShim implements Scope {
  final io.opentelemetry.context.Scope scope;

  public ScopeShim(io.opentelemetry.context.Scope scope) {
    this.scope = scope;
  }

  @Override
  public void close() {
    scope.close();
  }
}
