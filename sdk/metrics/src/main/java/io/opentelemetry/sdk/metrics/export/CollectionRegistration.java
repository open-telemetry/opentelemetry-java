/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;

/**
 * A {@link CollectionRegistration} is passed to each {@link MetricReader} registered with {@link
 * SdkMeterProvider}, and provides readers the ability to trigger metric collections.
 *
 * @since 1.14.0
 */
// TODO(jack-berg): Have methods when custom MetricReaders are supported
public interface CollectionRegistration {}
