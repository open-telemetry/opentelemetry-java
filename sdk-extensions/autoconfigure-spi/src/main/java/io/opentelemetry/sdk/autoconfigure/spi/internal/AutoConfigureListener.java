/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.OpenTelemetrySdk;

/**
 * Interface to be extended by SPIs that require access to the autoconfigured {@link
 * OpenTelemetrySdk} instance.
 *
 * <p>This is not a standalone SPI. Instead, implementations of other SPIs can also implement this
 * interface to receive a callback with the configured SDK.
 */
public interface AutoConfigureListener {

  void afterAutoConfigure(OpenTelemetrySdk sdk);
}
