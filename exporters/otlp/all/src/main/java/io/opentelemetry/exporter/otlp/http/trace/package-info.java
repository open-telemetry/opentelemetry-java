/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/** OpenTelemetry exporter which sends span data to OpenTelemetry collector via OTLP HTTP. */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.exporter.otlp.http.trace;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
