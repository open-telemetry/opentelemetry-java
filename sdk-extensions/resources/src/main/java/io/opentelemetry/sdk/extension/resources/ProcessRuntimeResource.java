/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.resources;

import static io.opentelemetry.sdk.resources.ResourceAttributes.PROCESS_RUNTIME_DESCRIPTION;
import static io.opentelemetry.sdk.resources.ResourceAttributes.PROCESS_RUNTIME_NAME;
import static io.opentelemetry.sdk.resources.ResourceAttributes.PROCESS_RUNTIME_VERSION;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceProvider;

/** {@link ResourceProvider} which provides information about the Java runtime. */
public final class ProcessRuntimeResource extends ResourceProvider {
  @Override
  protected Attributes getAttributes() {
    try {
      String name = System.getProperty("java.runtime.name");
      String version = System.getProperty("java.runtime.version");
      String description =
          System.getProperty("java.vm.vendor")
              + " "
              + System.getProperty("java.vm.name")
              + " "
              + System.getProperty("java.vm.version");

      return Attributes.of(
          PROCESS_RUNTIME_NAME,
          name,
          PROCESS_RUNTIME_VERSION,
          version,
          PROCESS_RUNTIME_DESCRIPTION,
          description);
    } catch (SecurityException ignored) {
      return Attributes.empty();
    }
  }
}
