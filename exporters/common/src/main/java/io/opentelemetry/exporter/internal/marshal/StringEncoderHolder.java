/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Factory and holder class for StringEncoder implementations.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
// public visibility only needed for benchmarking purposes
public final class StringEncoderHolder {
  private static final Logger logger = Logger.getLogger(StringEncoderHolder.class.getName());

  static final StringEncoder INSTANCE = createInstance();

  /**
   * Creates a FallbackStringEncoder instance.
   *
   * @return a new FallbackStringEncoder instance
   */
  public static StringEncoder createFallbackEncoder() {
    return new FallbackStringEncoder();
  }

  /**
   * Creates an UnsafeStringEncoder instance if available.
   *
   * @return an UnsafeStringEncoder instance if available, or null if not available
   */
  @Nullable
  public static StringEncoder createUnsafeEncoder() {
    return UnsafeStringEncoder.createIfAvailable();
  }

  /**
   * Creates a VarHandleStringEncoder instance if available.
   *
   * @return a VarHandleStringEncoder instance if available, or null if not available
   */
  @Nullable
  public static StringEncoder createVarHandleEncoder() {
    try {
      Class<?> varHandleClass =
          Class.forName("io.opentelemetry.exporter.internal.marshal.VarHandleStringEncoder");
      Method createMethod = varHandleClass.getMethod("createIfAvailable");
      return (StringEncoder) createMethod.invoke(null);
    } catch (Throwable t) {
      return null;
    }
  }

  private static StringEncoder createInstance() {
    // UnsafeStringEncoder has slightly better performance than VarHandleStringEncoder
    // so try it first
    if (!proactivelyAvoidUnsafe()) {
      StringEncoder unsafeImpl = createUnsafeEncoder();
      if (unsafeImpl != null) {
        logger.log(Level.FINE, "Using UnsafeStringEncoder for optimized Java 8+ performance");
        return unsafeImpl;
      }
    }

    // the VarHandle implementation requires --add-opens=java.base/java.lang=ALL-UNNAMED
    // for VarHandles to access String internals
    //
    // generally users won't do this and so won't get the VarHandle implementation
    // but the Java agent is able to automatically open these modules
    // (see ModuleOpener.java in that repository)
    StringEncoder varHandleImpl = createVarHandleEncoder();
    if (varHandleImpl != null) {
      logger.log(Level.FINE, "Using VarHandleStringEncoder for optimal Java 9+ performance");
      return varHandleImpl;
    }

    // Use fallback implementation
    logger.log(Level.FINE, "Using FallbackStringEncoder");
    return createFallbackEncoder();
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

  private StringEncoderHolder() {}
}
