/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import java.util.function.BiPredicate;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * State for a {@link SdkMeterProvider}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
public abstract class MeterProviderSharedState {

  public static MeterProviderSharedState create(
      Clock clock, SdkResourceProvider resourceProvider, ExemplarFilterInternal exemplarFilter) {
    return create(
        clock, resourceProvider, exemplarFilter, /* contextScopedAttributesFilter= */ null);
  }

  public static MeterProviderSharedState create(
      Clock clock,
      SdkResourceProvider resourceProvider,
      ExemplarFilterInternal exemplarFilter,
      @Nullable
          BiPredicate<InstrumentationScopeInfo, AttributeKey<?>> contextScopedAttributesFilter) {
    return new AutoValue_MeterProviderSharedState(
        clock, resourceProvider, exemplarFilter, contextScopedAttributesFilter);
  }

  MeterProviderSharedState() {}

  /** Returns the {@link Clock} used for measurements. */
  public abstract Clock getClock();

  /** Returns the {@link SdkResourceProvider} used to resolve the attached {@link Resource}. */
  public abstract SdkResourceProvider getSdkResourceProvider();

  /** Returns the {@link Resource} to attach telemetry to. */
  public Resource getResource() {
    return getSdkResourceProvider().getResource();
  }

  /** Returns the {@link ExemplarFilterInternal} for remembering measurements. */
  public abstract ExemplarFilterInternal getExemplarFilter();

  /**
   * Returns the filter predicate to determine which context-scoped attributes are inherited by
   * synchronous metric measurements, or {@code null} if disabled.
   */
  @Nullable
  public abstract BiPredicate<InstrumentationScopeInfo, AttributeKey<?>>
      getContextScopedAttributesFilter();
}
