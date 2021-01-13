/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** Base interface for all metrics defined in this package. */
@ThreadSafe
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface Instrument {}
