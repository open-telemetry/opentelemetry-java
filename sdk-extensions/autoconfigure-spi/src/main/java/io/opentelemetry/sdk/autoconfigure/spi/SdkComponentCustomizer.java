/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

/**
 * A customizer of an SDK component, for use with {@link AutoConfiguredOpenTelemetrySdkCustomizer}.
 */
@FunctionalInterface
public interface SdkComponentCustomizer<I, O> {

  /** Returns a customized component. */
  O apply(I configured, ConfigProperties config);
}
