/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.trace;

public class WireFormatUtils {

  private WireFormatUtils() {}

  /**
   * Returns whether the version is valid. A valid version is 1 byte representing an 8-bit unsigned
   * integer, version ff is invalid.
   *
   * @return {@code true} if the version is valid.
   */
  public static boolean isValidVersion(String version) {
    return version.length() == 2
        && BigendianEncoding.isValidBase16String(version)
        && !version.equals("ff");
  }
}
