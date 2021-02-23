/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Optional;
import javax.annotation.concurrent.Immutable;

@Immutable
interface B3PropagatorExtractor {

  <C> Optional<Context> extract(Context context, C carrier, TextMapGetter<C> getter);
}
