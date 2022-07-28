/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * {@link io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider} implementations for inferring
 * resource information for AWS services.
 */
@ParametersAreNonnullByDefault
@Export
package io.opentelemetry.sdk.extension.aws.resource;

import org.osgi.annotation.bundle.Export;
import javax.annotation.ParametersAreNonnullByDefault;
