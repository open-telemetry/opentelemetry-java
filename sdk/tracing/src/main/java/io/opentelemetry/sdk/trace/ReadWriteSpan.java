/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Span;

/**
 * A combination of the write methods from the {@link Span} interface and the read methods from the
 * {@link ReadableSpan} interface.
 */
public interface ReadWriteSpan extends Span, ReadableSpan {}
