/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import org.jctools.queues.MpscCompoundQueue;

public final class JcTools {

  public static <T> MpscCompoundQueue<T> newMpscCompoundQueue(int capacity) {
    return new MpscCompoundQueue<>(capacity);
  }

  private JcTools() {}
}
