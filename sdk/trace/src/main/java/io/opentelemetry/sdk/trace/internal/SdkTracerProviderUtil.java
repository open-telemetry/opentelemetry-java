/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessorBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * A collection of methods that allow use of experimental features prior to availability in public
 * APIs.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkTracerProviderUtil {

  private SdkTracerProviderUtil() {}

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkTracerProvider}. */
  public static void setTracerConfigurator(
      SdkTracerProvider sdkTracerProvider, ScopeConfigurator<TracerConfig> scopeConfigurator) {
    try {
      Method method =
          SdkTracerProvider.class.getDeclaredMethod(
              "setTracerConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkTracerProvider, scopeConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setTracerConfigurator on SdkTracerProvider", e);
    }
  }

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

  /** Reflectively set exception attribute resolver to the {@link SdkTracerProviderBuilder}. */
  public static void setExceptionAttributeResolver(
      SdkTracerProviderBuilder sdkTracerProviderBuilder,
      ExceptionAttributeResolver exceptionAttributeResolver) {
    try {
      Method method =
          SdkTracerProviderBuilder.class.getDeclaredMethod(
              "setExceptionAttributeResolver", ExceptionAttributeResolver.class);
      method.setAccessible(true);
      method.invoke(sdkTracerProviderBuilder, exceptionAttributeResolver);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setExceptionAttributeResolver on SdkTracerProviderBuilder", e);
    }
  }

  /** Reflectively set meter provider to the {@link SdkTracerProviderBuilder}. */
  public static SdkTracerProviderBuilder setMeterProvider(
      SdkTracerProviderBuilder sdkTracerProviderBuilder, MeterProvider meterProvider) {
    try {
      Method method =
          SdkTracerProviderBuilder.class.getDeclaredMethod("setMeterProvider", MeterProvider.class);
      method.setAccessible(true);
      method.invoke(sdkTracerProviderBuilder, meterProvider);
      return sdkTracerProviderBuilder;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setMeterProvider on SdkTracerProviderBuilder", e);
    }
  }

  /** Reflectively set meter provider to the {@link SdkTracerProviderBuilder}. */
  public static SimpleSpanProcessorBuilder setMeterProvider(
      SimpleSpanProcessorBuilder simpleSpanProcessorBuilder, MeterProvider meterProvider) {
    try {
      Method method =
          SimpleSpanProcessorBuilder.class.getDeclaredMethod(
              "setMeterProvider", MeterProvider.class);
      method.setAccessible(true);
      method.invoke(simpleSpanProcessorBuilder, meterProvider);
      return simpleSpanProcessorBuilder;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setMeterProvider on SimpleSpanProcessorBuilder", e);
    }
  }
}
