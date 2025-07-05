/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import io.opentelemetry.api.incubator.entities.Resource;

/**
 * The Resource detector in the SDK is responsible for detecting possible entities that could
 * identify the SDK (called "associated entities"). For Example, if the SDK is running in a
 * kubernetes pod, it may provide an Entity for that pod.
 */
public interface ResourceDetector {
  /**
   * Configures a resource with detected entities.
   *
   * @param resource The resource to detect entities on.
   */
  public void configure(Resource resource);
}
