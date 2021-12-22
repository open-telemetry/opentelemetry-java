/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/**
 * A reference to an observable metric instrument registered with a {@code buildWithCallback}
 * method.
 *
 * <p>This interface currently has no methods but may be extended in the future with functionality
 * such as canceling the observable.
 */
public interface Observable {}
