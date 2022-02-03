/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;

/**
 * A reference to an observable metric registered with {@link
 * LongCounterBuilder#buildWithCallback(Consumer)}.
 *
 * <p>This interface currently has no methods but may be extended in the future with functionality
 * such as canceling the observable.
 */
public interface ObservableLongCounter extends ObservableInstrument {}
