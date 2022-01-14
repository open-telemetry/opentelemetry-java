/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * This package describes the Metrics API that can be used to record application Metrics.
 *
 * <p>The primary entry point to Metrics is the {@link io.opentelemetry.api.metrics.MeterProvider},
 * which allows the construction of a {@link io.opentelemetry.api.metrics.Meter}. Instrumentated
 * libraries should construct a single {@link io.opentelemetry.api.metrics.Meter} and register
 * `instruments` via the builders available on {@link io.opentelemetry.api.metrics.Meter}.
 *
 * <p>There is a global instance of {@link io.opentelemetry.api.metrics.MeterProvider} available for
 * scenarios where instrumentation authors are unable to obtain one by other means.
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.api.metrics;

import javax.annotation.ParametersAreNonnullByDefault;
