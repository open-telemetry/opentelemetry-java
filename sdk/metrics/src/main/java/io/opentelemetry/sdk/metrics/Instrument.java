/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

/**
 * Placeholder to avoid modifying SDK implementation.
 *
 * <p>Previously, this was defined in the API. However, the new API for asynchronous instruments
 * returns no value to the user, as there's nothing the user can alter (yet).
 */
interface Instrument {}
