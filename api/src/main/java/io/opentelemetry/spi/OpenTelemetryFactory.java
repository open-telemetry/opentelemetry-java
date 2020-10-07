/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.spi;

import io.opentelemetry.OpenTelemetry;

public interface OpenTelemetryFactory {

  OpenTelemetry create();
}
