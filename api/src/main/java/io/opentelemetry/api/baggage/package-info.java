/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * API for associating entries with scoped operations.
 *
 * <p>This package manages a set of entries in the {@link io.opentelemetry.context.Context}. The
 * entries can be used to label anything that is associated with a specific operation. For example,
 * the {@code opentelemetry.stats} package labels all stats with the current entries.
 *
 * <p>{@link io.opentelemetry.api.baggage.Entry Entrys} are key-value pairs of {@link
 * java.lang.String}s. They are stored as a map in a {@link io.opentelemetry.api.baggage.Baggage}.
 *
 * <p>Note that entries are independent of the tracing data that is propagated in the {@link
 * io.opentelemetry.context.Context}, such as trace ID.
 */
// TODO: Add code examples.
@ParametersAreNonnullByDefault
package io.opentelemetry.api.baggage;

import javax.annotation.ParametersAreNonnullByDefault;
