/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import java.lang.management.ManagementFactory;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

final class ProcessPid {

  private ProcessPid() {}

  @IgnoreJRERequirement
  static long getPid() {
    // While this is not strictly defined, almost all commonly used JVMs format this as
    // pid@hostname.
    String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
    int atIndex = runtimeName.indexOf('@');
    if (atIndex >= 0) {
      String pidString = runtimeName.substring(0, atIndex);
      try {
        return Long.parseLong(pidString);
      } catch (NumberFormatException ignored) {
        // Ignore parse failure.
      }
    }
    return -1;
  }
}
