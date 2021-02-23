/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import javax.annotation.concurrent.Immutable;

@Immutable
interface B3PropagatorInjector {
  <C> void inject(Context context, C carrier, TextMapSetter<C> setter);
}
