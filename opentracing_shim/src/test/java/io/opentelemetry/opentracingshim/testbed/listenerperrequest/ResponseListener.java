/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim.testbed.listenerperrequest;

import io.opentracing.Span;

/** Response listener per request. Executed in a thread different from 'send' thread */
final class ResponseListener {
  private final Span span;

  public ResponseListener(Span span) {
    this.span = span;
  }

  /** executed when response is received from server. Any thread. */
  public void onResponse(Object response) {
    span.finish();
  }
}
