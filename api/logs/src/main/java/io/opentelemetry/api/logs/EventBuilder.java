/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

/**
 * Used to construct an emit events from a {@link Logger}.
 *
 * <p>An event is a log record with attributes for {@code event.domain} and {@code event.name}.
 *
 * <p>Obtain a {@link Logger#eventBuilder(String)}, add properties using the setters, and emit the
 * log record by calling {@link #emit()}.
 */
public interface EventBuilder extends LogRecordBuilder {}
