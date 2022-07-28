/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Capture Spans and Scopes as events in JFR recordings.
 *
 * @see io.opentelemetry.sdk.extension.jfr.JfrSpanProcessor
 */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.sdk.extension.jfr;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
