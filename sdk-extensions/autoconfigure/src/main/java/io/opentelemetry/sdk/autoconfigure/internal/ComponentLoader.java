/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

/**
 * A loader for components that are discovered via SPI.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @deprecated Use {@link io.opentelemetry.common.ComponentLoader} instead
 */
@Deprecated
// TODO(jack-berg): delete after 1.54.0 release
public interface ComponentLoader extends io.opentelemetry.common.ComponentLoader {}
