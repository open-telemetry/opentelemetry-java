/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

public interface ValueListBuilder {

  ValueListBuilder add(Value<?> value);

  Value<?> build();
}
