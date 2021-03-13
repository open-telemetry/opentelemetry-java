/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

/** Factory of a {@link Resource} which provides information about the current running process. */
public final class ProcessResource {

  private static final Resource INSTANCE = buildResource();

  /**
   * Returns a factory for a {@link Resource} which provides information about the current running
   * process.
   */
  public static Resource get() {
    return INSTANCE;
  }

  // Visible for testing
  static Resource buildResource() {
    try {
      return doBuildResource();
    } catch (LinkageError t) {
      // Will only happen on Android, where these attributes generally don't make much sense
      // anyways.
      return Resource.empty();
    }
  }

  @IgnoreJRERequirement
  private static Resource doBuildResource() {
    AttributesBuilder attributes = Attributes.builder();

    // TODO(anuraaga): Use reflection to get more stable values on Java 9+
    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
    long pid = -1;
    // While this is not strictly defined, almost all commonly used JVMs format this as
    // pid@hostname.
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

    if (pid >= 0) {
      attributes.put(ResourceAttributes.PROCESS_PID, pid);
    }

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

      attributes.put(ResourceAttributes.PROCESS_EXECUTABLE_PATH, executablePath.toString());

      StringBuilder commandLine = new StringBuilder(executablePath);
      for (String arg : runtime.getInputArguments()) {
        commandLine.append(' ').append(arg);
      }
      attributes.put(ResourceAttributes.PROCESS_COMMAND_LINE, commandLine.toString());
    }

    return Resource.create(attributes.build());
  }

  private ProcessResource() {}
}
