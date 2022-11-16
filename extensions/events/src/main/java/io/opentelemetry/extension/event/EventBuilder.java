/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.event;

import io.opentelemetry.api.logs.LogRecordBuilder;

/**
 * Used to construct an emit Events from a {@link EventLogger}.
 *
 * <p>An Event is a {@code LogRecord} that conform <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/semantic_conventions/events.md">Event
 * Semantic Conventions</a>.
 *
 * <p>Obtain a {@link EventLogger#eventBuilder(String)}, add properties using the setters, and emit
 * the log record by calling {@link #emit()}.
 */
public interface EventBuilder extends LogRecordBuilder {}
