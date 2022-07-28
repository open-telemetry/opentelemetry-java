/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/** OpenTelemetry exporter which sends metric data to OpenTelemetry collector via OTLP gRPC. */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.exporter.otlp.metrics;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
