/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;

public final class BlockingSpanExporter implements SpanExporter {

  final Object monitor = new Object();

  private enum State {
    WAIT_TO_BLOCK,
    BLOCKED,
    UNBLOCKED
  }

  @GuardedBy("monitor")
  State state = State.WAIT_TO_BLOCK;

  @Override
  public CompletableResultCode export(Collection<SpanData> spanDataList) {
    synchronized (monitor) {
      while (state != State.UNBLOCKED) {
        try {
          state = State.BLOCKED;
          // Some threads may wait for Blocked State.
          monitor.notifyAll();
          monitor.wait();
        } catch (InterruptedException e) {
          // Do nothing
        }
      }
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  public void waitUntilIsBlocked() {
    synchronized (monitor) {
      while (state != State.BLOCKED) {
        try {
          monitor.wait();
        } catch (InterruptedException e) {
          // Do nothing
        }
      }
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    // Do nothing;
    return CompletableResultCode.ofSuccess();
  }

  public void unblock() {
    synchronized (monitor) {
      state = State.UNBLOCKED;
      monitor.notifyAll();
    }
  }
}
