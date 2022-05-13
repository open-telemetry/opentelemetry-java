/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.internal.debug.SourceInfo;
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
      InstrumentSelector selector,
      View view,
      AttributesProcessor viewAttributesProcessor,
      SourceInfo viewSourceInfo) {
    return new AutoValue_RegisteredView(selector, view, viewAttributesProcessor, viewSourceInfo);
  }

  RegisteredView() {}

  /** Instrument filter for applying this view. */
  public abstract InstrumentSelector getInstrumentSelector();

  /** The view to apply. */
  public abstract View getView();

  /** The view's {@link AttributesProcessor}. */
  public abstract AttributesProcessor getViewAttributesProcessor();

  /** The {@link SourceInfo} from where the view was registered. */
  public abstract SourceInfo getViewSourceInfo();

  @Override
  public final String toString() {
    return "RegisteredView{"
        + "instrumentSelector="
        + getInstrumentSelector()
        + ", view="
        + getView()
        + "}";
  }
}
