/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * An exporter that uses the <a href="https://lmax-exchange.github.io/disruptor/">LMAX Disruptor</a>
 * for processing exported spans.
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.sdk.extension.trace.export;

import javax.annotation.ParametersAreNonnullByDefault;
