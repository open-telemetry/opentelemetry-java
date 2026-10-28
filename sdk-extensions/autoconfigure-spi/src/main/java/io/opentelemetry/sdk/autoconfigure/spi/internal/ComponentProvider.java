/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

/**
 * Provides configured instances of SDK extension components.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @deprecated Use {@link io.opentelemetry.sdk.autoconfigure.spi.impl.ComponentProvider} instead.
 */
@Deprecated
public interface ComponentProvider
    extends io.opentelemetry.sdk.autoconfigure.spi.impl.ComponentProvider {}
