/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class ThrowableUtil {

  /**
   * Throw the {@link Throwable} if fatal.
   *
   * <p>Taken from RxJava throwIfFatal, which was taken from scala.
   */
  public static void propagateIfFatal(Throwable t) {
    if (t instanceof VirtualMachineError) {
      throw (VirtualMachineError) t;
    } else if (t instanceof ThreadDeath) {
      throw (ThreadDeath) t;
    } else if (t instanceof LinkageError) {
      throw (LinkageError) t;
    }
  }

  private ThrowableUtil() {}
}
