/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@IgnoreJRERequirement
public final class AdderUtil {

  private static final boolean hasJreAdder;

  static {
    boolean jreAdder = true;
    try {
      Class.forName("java.util.concurrent.atomic.DoubleAdder");
      Class.forName("java.util.concurrent.atomic.LongAdder");
    } catch (ClassNotFoundException e) {
      jreAdder = false;
    }
    hasJreAdder = jreAdder;
  }

  /**
   * Create an instance of {@link LongAdder}. The implementation will be {@link
   * java.util.concurrent.atomic.LongAdder} if available on the classpath. If not, a less performant
   * implementation based on {@link AtomicLong} will be used.
   */
  public static LongAdder createLongAdder() {
    return hasJreAdder ? new JreLongAdder() : new AtomicLongLongAdder();
  }

  /**
   * Create an instance of {@link LongAdder}. The implementation will be {@link
   * java.util.concurrent.atomic.DoubleAdder} if available on the classpath. If not, a less
   * performant implementation based on {@link AtomicLong} will be used.
   */
  public static DoubleAdder createDoubleAdder() {
    return hasJreAdder ? new JreDoubleAdder() : new AtomicLongDoubleAdder();
  }

  private AdderUtil() {}
}
