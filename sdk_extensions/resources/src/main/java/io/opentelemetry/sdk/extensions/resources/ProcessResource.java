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

package io.opentelemetry.sdk.extensions.resources;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceConstants;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/** {@link ResourceProvider} which provides information about the current running process. */
public class ProcessResource extends ResourceProvider {
  @Override
  protected Attributes getAttributes() {
    Attributes.Builder attributes = Attributes.newBuilder();

    // TODO(anuraaga): Use reflection to get more stable values on Java 9+
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    long pid = -1;
    String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
    int atIndex = runtimeName.indexOf('@');
    if (atIndex >= 0) {
      String pidString = runtimeName.substring(0, atIndex);
      try {
        pid = Long.parseLong(pidString);
      } catch (NumberFormatException ignored) {
        // Ignore parse failure.
      }
    }

    attributes.setAttribute(ResourceConstants.PROCESS_PID, pid);

    String javaHome = null;
    String osName = null;
    try {
      javaHome = System.getProperty("java.home");
      osName = System.getProperty("os.name");
    } catch (SecurityException e) {
      // Ignore
    }
    if (javaHome != null) {
      StringBuilder executablePath = new StringBuilder(javaHome);
      executablePath
          .append(File.pathSeparatorChar)
          .append("bin")
          .append(File.pathSeparatorChar)
          .append("java");
      if (osName != null && osName.toLowerCase().startsWith("windows")) {
        executablePath.append(".exe");
      }

      attributes.setAttribute(ResourceConstants.PROCESS_EXECUTABLE_PATH, executablePath.toString());

      StringBuilder commandLine = new StringBuilder(executablePath);
      for (String arg : runtime.getInputArguments()) {
        commandLine.append(' ').append(arg);
      }
      attributes.setAttribute(ResourceConstants.PROCESS_COMMAND_LINE, commandLine.toString());
    }

    return attributes.build();
  }
}
