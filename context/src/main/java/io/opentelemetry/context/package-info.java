/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * A context propagation mechanism which can carry scoped-values across API boundaries and between
 * threads.
 *
 * @see io.opentelemetry.context.Context
 */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.context;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
