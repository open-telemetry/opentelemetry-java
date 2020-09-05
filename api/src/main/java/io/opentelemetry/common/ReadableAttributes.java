/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

/**
 * A read-only container for String-keyed attributes.
 *
 * <p>See {@link Attributes} for the public API implementation.
 */
public interface ReadableAttributes extends ReadableKeyValuePairs<AttributeValue> {}
