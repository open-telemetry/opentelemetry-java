/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources;

import java.util.List;

/**
 * The Entity detector in the SDK is responsible for detecting possible entities that could identify
 * the SDK (called "associated entities"). For Example, if the SDK is running in a kubernetes pod,
 * it may provide an Entity for that pod.
 */
public interface EntityDetector {
  List<Entity> detectEntities();
}
