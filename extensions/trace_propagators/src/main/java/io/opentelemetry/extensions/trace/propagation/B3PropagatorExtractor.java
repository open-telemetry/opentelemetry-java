/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.trace.propagation;

import io.grpc.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
interface B3PropagatorExtractor {

  @Nullable
  <C> Context extract(Context context, C carrier, TextMapPropagator.Getter<C> getter);
}
