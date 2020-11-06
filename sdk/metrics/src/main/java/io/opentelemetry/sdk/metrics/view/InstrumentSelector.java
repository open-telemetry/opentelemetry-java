/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */


package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class InstrumentSelector {

  public static Builder newBuilder() {
    return new AutoValue_InstrumentSelector.Builder();
  }

  @Nullable
  public abstract InstrumentType instrumentType();

  @Nullable
  public abstract String instrumentNameRegex();

  @Memoized
  @Nullable
  public Pattern instrumentNamePattern() {
    return instrumentNameRegex() == null ? null : Pattern.compile(instrumentNameRegex());
  }

  @AutoValue.Builder
  public interface Builder {
    Builder instrumentType(InstrumentType instrumentType);

    Builder instrumentNameRegex(String regex);

    InstrumentSelector build();
  }
}