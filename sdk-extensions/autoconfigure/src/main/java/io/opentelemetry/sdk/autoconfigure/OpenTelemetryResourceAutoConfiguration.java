/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nullable;

/** Auto-configuration for the OpenTelemetry {@link Resource}. */
public final class OpenTelemetryResourceAutoConfiguration {

  @Nullable private static volatile Resource resource = null;

  private static final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private static Throwable initializeCaller = null;

  /**
   * Returns the automatically configured {@link Resource}.
   *
   * <p>This method will auto-configure the returned {@link Resource} using system properties and
   * environment variables if none of the {@code initialize()} methods have been called before.
   */
  public static Resource getResource() {
    if (resource == null) {
      synchronized (lock) {
        if (resource == null) {
          // auto-initialize using system properties & environment variables
          initialize();
        }
      }
    }
    return resource;
  }

  /**
   * Returns a {@link Resource} automatically initialized through recognized system properties and
   * environment variables.
   *
   * <p>This will automatically set the resulting resource as the global instance returned by {@link
   * #getResource()}.
   */
  public static Resource initialize() {
    return initialize(/* setResultAsGlobal= */ true, DefaultConfigProperties.get());
  }

  /**
   * Returns a {@link Resource} automatically initialized through recognized system properties and
   * environment variables.
   *
   * @param setResultAsGlobal Whether to automatically set the configured resource as the global
   *     instance returned by {@link #getResource()}.
   */
  public static Resource initialize(boolean setResultAsGlobal, ConfigProperties config) {
    Resource resource = buildResource(config);
    if (setResultAsGlobal) {
      set(resource);
    }
    return resource;
  }

  private static Resource buildResource(ConfigProperties config) {
    Resource result = Resource.getDefault();

    // TODO(anuraaga): We use a hyphen only once in this artifact, for
    // otel.java.disabled.resource-providers. But fetching by the dot version is the simplest way
    // to implement it for now.
    Set<String> disabledProviders =
        new HashSet<>(config.getCommaSeparatedValues("otel.java.disabled.resource.providers"));
    for (ResourceProvider resourceProvider : ServiceLoader.load(ResourceProvider.class)) {
      if (disabledProviders.contains(resourceProvider.getClass().getName())) {
        continue;
      }
      result = result.merge(resourceProvider.createResource(config));
    }

    result = result.merge(EnvironmentResource.create(config));

    return result;
  }

  private static void set(Resource r) {
    synchronized (lock) {
      if (resource != null) {
        throw new IllegalStateException(
            "OpenTelemetryResourceAutoConfiguration.initialize() or initialize(true, ...) has"
                + " already been called. You can only initialize the global resource once. Previous"
                + " invocation set to cause of this exception.",
            initializeCaller);
      }
      resource = r;
      initializeCaller = new Throwable();
    }
  }

  static void resetForTest() {
    synchronized (lock) {
      resource = null;
      initializeCaller = null;
    }
  }

  private OpenTelemetryResourceAutoConfiguration() {}
}
