/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;

final class ClasspathUtil {

  static boolean checkClassExists(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException unused) {
      return false;
    }
  }

  @SuppressWarnings("UnusedException")
  static void checkClassExists(String className, String featureName, String requiredLibrary) {
    if (!checkClassExists(className)) {
      throw new ConfigurationException(
          featureName
              + " enabled but "
              + requiredLibrary
              + " not found on classpath. "
              + "Make sure to add it as a dependency to enable this feature.");
    }
  }

  private ClasspathUtil() {}
}
