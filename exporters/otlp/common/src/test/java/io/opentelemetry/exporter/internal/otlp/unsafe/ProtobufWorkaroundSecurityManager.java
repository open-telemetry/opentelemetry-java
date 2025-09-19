/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.unsafe;

import java.security.Permission;

public class ProtobufWorkaroundSecurityManager extends SecurityManager {

  @Override
  public void checkPermission(Permission perm) {
    // Block access to sun.misc.Unsafe fields
    if (perm instanceof java.lang.reflect.ReflectPermission) {
      if ("suppressAccessChecks".equals(perm.getName())) {
        // Get the stack trace to see what's trying to access
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
          if (element.getClassName().contains("UnsafeUtil")
              && element.getMethodName().contains("getUnsafe")) {
            throw new SecurityException("Access to sun.misc.Unsafe denied");
          }
        }
      }
    }
    // Allow everything else
  }

  @Override
  public void checkPermission(Permission perm, Object context) {
    checkPermission(perm);
  }
}
