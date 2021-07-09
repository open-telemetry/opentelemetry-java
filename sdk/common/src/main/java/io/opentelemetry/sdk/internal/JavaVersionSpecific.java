/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2019 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.opentelemetry.sdk.internal;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains APIs that are implemented differently based on the version of Java being run. This class
 * implements the default, using Java 8 APIs, the minimum version supported by OpenTelemetry. All
 * implementations in this class must be forwards-compatible on all Java versions because this class
 * may be used outside the multi-release JAR, e.g., in testing or when a user shades without
 * creating their own multi-release JAR.
 */
public class JavaVersionSpecific {

  private static final Logger logger = Logger.getLogger(JavaVersionSpecific.class.getName());

  private static final JavaVersionSpecific CURRENT = CurrentJavaVersionSpecific.get();

  static {
    if (CURRENT.getClass() != JavaVersionSpecific.class) {
      logger.log(Level.FINE, "Using the APIs optimized for: {0}", CURRENT.name());
    }
  }

  /** Returns the {@link JavaVersionSpecific} for the current version of Java. */
  public static JavaVersionSpecific get() {
    return CURRENT;
  }

  String name() {
    return "Java 8";
  }

  /** Returns the number of nanoseconds since the epoch (00:00:00, 01-Jan-1970, GMT). */
  public long currentTimeNanos() {
    return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  }
}
