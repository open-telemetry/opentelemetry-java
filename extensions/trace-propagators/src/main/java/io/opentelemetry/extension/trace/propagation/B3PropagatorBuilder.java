/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

/**
 * Enables the creation of an {@link B3Propagator} instance with the ability to switch injector from
 * single header (default) to multiple headers.
 */
public final class B3PropagatorBuilder {
  private boolean injectSingleHeader;

  B3PropagatorBuilder() {
    injectSingleHeader = true;
  }

  public B3PropagatorBuilder injectMultipleHeaders() {
    this.injectSingleHeader = false;
    return this;
  }

  /** Create the {@link B3Propagator} with selected injector type. */
  public B3Propagator build() {
    if (injectSingleHeader) {
      return new B3Propagator(new B3PropagatorInjectorSingleHeader());
    } else {
      return new B3Propagator(new B3PropagatorInjectorMultipleHeaders());
    }
  }
}
