/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

/**
 * Constants for experimental entity SDK features.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class EntityExperimentConstants {

  /** The configuration key for enabling experimental entity support in resource detectors. */
  public static final String EXPERIMENTAL_ENTITIES_ENABLED = "otel.experimental.entities.enabled";

  private EntityExperimentConstants() {}
}
