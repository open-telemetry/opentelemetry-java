/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;

/**
 * Internal accessor of JCTools package for fast queues.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JcTools {

  private static final AtomicBoolean queueCreationWarningLogged = new AtomicBoolean();
  private static final Logger logger = Logger.getLogger(JcTools.class.getName());
  private static final boolean PROACTIVELY_AVOID_UNSAFE = proactivelyAvoidUnsafe();

  /**
   * Returns a new {@link Queue} appropriate for use with multiple producers and a single consumer.
   */
  public static <T> Queue<T> newFixedSizeQueue(int capacity) {
    if (PROACTIVELY_AVOID_UNSAFE) {
      return new MpscAtomicArrayQueue<>(capacity);
    }
    try {
      return new MpscArrayQueue<>(capacity);
    } catch (java.lang.NoClassDefFoundError | java.lang.ExceptionInInitializerError e) {
      if (!queueCreationWarningLogged.getAndSet(true)) {
        logger.log(
            Level.WARNING,
            "Cannot create high-performance queue, reverting to ArrayBlockingQueue ({0})",
            Objects.toString(e, "unknown cause"));
      }
      // Happens when modules such as jdk.unsupported are disabled in a custom JRE distribution,
      // or a security manager preventing access to Unsafe is installed.
      return new MpscAtomicArrayQueue<>(capacity);
    }
  }

  /**
   * Returns the capacity of the {@link Queue}. We cast to the implementation so callers do not need
   * to use the shaded classes.
   */
  public static long capacity(Queue<?> queue) {
    return ((MessagePassingQueue<?>) queue).capacity();
  }

  /**
   * Remove up to <i>limit</i> elements from the {@link Queue} and hand to consume.
   *
   * @throws IllegalArgumentException consumer is {@code null}
   * @throws IllegalArgumentException if maxExportBatchSize is negative
   */
  @SuppressWarnings("unchecked")
  public static <T> int drain(Queue<T> queue, int limit, Consumer<T> consumer) {
    return ((MessagePassingQueue<T>) queue).drain(consumer::accept, limit);
  }

  private static boolean proactivelyAvoidUnsafe() {
    Optional<Double> javaVersion = getJavaVersion();
    // Avoid Unsafe on Java 23+ due to JEP-498 deprecation warnings:
    // "WARNING: A terminally deprecated method in sun.misc.Unsafe has been called"
    return javaVersion.map(version -> version >= 23).orElse(true);
  }

  private static Optional<Double> getJavaVersion() {
    String specVersion = System.getProperty("java.specification.version");
    if (specVersion != null) {
      try {
        return Optional.of(Double.parseDouble(specVersion));
      } catch (NumberFormatException exception) {
        // ignore
      }
    }
    return Optional.empty();
  }

  private JcTools() {}
}
