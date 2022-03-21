/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import javax.annotation.concurrent.Immutable;

/**
 * Internal representation of a {@link View} and {@link InstrumentSelector}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class RegisteredView {

  public static RegisteredView create(
      InstrumentSelector selector, View view, SourceInfo viewSourceInfo) {
    return new AutoValue_RegisteredView(selector, view, viewSourceInfo);
  }

  RegisteredView() {}

  /** Instrument filter for applying this view. */
  public abstract InstrumentSelector getInstrumentSelector();

  /** The view to apply. */
  public abstract View getView();

  /** The {@link SourceInfo} from where the view was registered. */
  public abstract SourceInfo getViewSourceInfo();
}
