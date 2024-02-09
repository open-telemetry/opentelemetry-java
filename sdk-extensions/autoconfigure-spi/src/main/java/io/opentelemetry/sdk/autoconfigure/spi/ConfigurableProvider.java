/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

/**
 * A named configurable provider.
 *
 * <p>It can be used to generically determine if a provider should be replaced by another provider
 * with the same name.
 */
public interface ConfigurableProvider {
  /** Returns the name of this provider. */
  String getName();
}
