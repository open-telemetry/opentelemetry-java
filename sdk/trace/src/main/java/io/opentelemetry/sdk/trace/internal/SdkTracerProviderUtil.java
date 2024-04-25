/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * A collection of methods that allow use of experimental features prior to availability in public
 * APIs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SdkTracerProviderUtil {

  private SdkTracerProviderUtil() {}

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkTracerProviderBuilder}. */
  public static void setTracerConfigurator(
      SdkTracerProviderBuilder sdkTracerProviderBuilder,
      ScopeConfigurator<TracerConfig> tracerConfigurator) {
    try {
      Method method =
          SdkTracerProviderBuilder.class.getDeclaredMethod(
              "setTracerConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkTracerProviderBuilder, tracerConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setTracerConfigurator on SdkTracerProviderBuilder", e);
    }
  }

  /** Reflectively add a tracer configurator condition to the {@link SdkTracerProviderBuilder}. */
  public static void addTracerConfiguratorCondition(
      SdkTracerProviderBuilder sdkTracerProviderBuilder,
      Predicate<InstrumentationScopeInfo> scopeMatcher,
      TracerConfig tracerConfig) {
    try {
      Method method =
          SdkTracerProviderBuilder.class.getDeclaredMethod(
              "addTracerConfiguratorCondition", Predicate.class, TracerConfig.class);
      method.setAccessible(true);
      method.invoke(sdkTracerProviderBuilder, scopeMatcher, tracerConfig);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling addTracerConfiguratorCondition on SdkTracerProviderBuilder", e);
    }
  }
}
