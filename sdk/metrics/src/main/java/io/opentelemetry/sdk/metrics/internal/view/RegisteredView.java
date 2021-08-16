/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import javax.annotation.concurrent.Immutable;

/** Internal representation of a {@link View} and {@link InstrumentSelector}. */
@AutoValue
@Immutable
abstract class RegisteredView {
  /** Instrument fitler for applying this view. */
  public abstract InstrumentSelector getInstrumentSelector();
  /** The view to apply. */
  public abstract View getView();

  static RegisteredView create(InstrumentSelector selector, View view) {
    return new AutoValue_RegisteredView(selector, view);
  }
}
