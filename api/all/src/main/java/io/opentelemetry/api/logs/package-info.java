/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * API for writing log appenders.
 *
 * <p>The OpenTelemetry logging API exists to enable the creation of log appenders, which bridge
 * logs from other log frameworks (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry via
 * {@link io.opentelemetry.api.logs.Logger#logRecordBuilder()}. It is <b>NOT</b> a replacement log
 * framework.
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.api.logs;

import javax.annotation.ParametersAreNonnullByDefault;
