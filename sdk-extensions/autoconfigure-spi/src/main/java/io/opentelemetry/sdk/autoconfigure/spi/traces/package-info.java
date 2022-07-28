/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Java SPI (Service Provider Interface) for implementing extensions to SDK autoconfiguration of
 * traces.
 */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.sdk.autoconfigure.spi.traces;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
