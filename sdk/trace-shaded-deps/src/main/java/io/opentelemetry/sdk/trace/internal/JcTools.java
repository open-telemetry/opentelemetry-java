/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Queue;
import org.jctools.queues.MpscCompoundQueue;

public final class JcTools {

  public static <T> Queue<T> newMpscCompoundQueue(int capacity) {
    return new MpscCompoundQueue<>(capacity);
  }

  private JcTools() {}
}
