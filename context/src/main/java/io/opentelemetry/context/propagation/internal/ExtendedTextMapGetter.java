/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context.propagation.internal;

import io.opentelemetry.context.propagation.TextMapGetter;

/**
 * Extended {@link TextMapGetter} with experimental APIs.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedTextMapGetter<C> extends TextMapGetter<C> {

  // keep this class even if it is empty, since experimental methods may be added in the future.

}
