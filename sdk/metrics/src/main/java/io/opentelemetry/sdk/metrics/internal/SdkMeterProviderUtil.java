/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.ViewBuilder;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.StringPredicates;
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
public final class SdkMeterProviderUtil {

  private SdkMeterProviderUtil() {}

  /**
   * Reflectively assign the {@link ExemplarFilter} to the {@link SdkMeterProviderBuilder}.
   *
   * @param sdkMeterProviderBuilder the builder
   */
  public static void setExemplarFilter(
      SdkMeterProviderBuilder sdkMeterProviderBuilder, ExemplarFilter exemplarFilter) {
    try {
      Method method =
          SdkMeterProviderBuilder.class.getDeclaredMethod(
              "setExemplarFilter", ExemplarFilter.class);
      method.setAccessible(true);
      method.invoke(sdkMeterProviderBuilder, exemplarFilter);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setExemplarFilter on SdkMeterProviderBuilder", e);
    }
  }

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkMeterProviderBuilder}. */
  public static void setMeterConfigurator(
      SdkMeterProviderBuilder sdkMeterProviderBuilder,
      ScopeConfigurator<MeterConfig> meterConfigurator) {
    try {
      Method method =
          SdkMeterProviderBuilder.class.getDeclaredMethod(
              "setMeterConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkMeterProviderBuilder, meterConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setMeterConfigurator on SdkMeterProviderBuilder", e);
    }
  }

  /** Reflectively add a tracer configurator condition to the {@link SdkMeterProviderBuilder}. */
  public static void addMeterConfiguratorCondition(
      SdkMeterProviderBuilder sdkMeterProviderBuilder,
      Predicate<InstrumentationScopeInfo> scopeMatcher,
      MeterConfig meterConfig) {
    try {
      Method method =
          SdkMeterProviderBuilder.class.getDeclaredMethod(
              "addMeterConfiguratorCondition", Predicate.class, MeterConfig.class);
      method.setAccessible(true);
      method.invoke(sdkMeterProviderBuilder, scopeMatcher, meterConfig);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling addMeterConfiguratorCondition on SdkMeterProviderBuilder", e);
    }
  }

  /**
   * Reflectively add an {@link AttributesProcessor} to the {@link ViewBuilder} which appends
   * key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param viewBuilder the builder
   * @param keyFilter Only baggage key values pairs where the key matches this predicate will be
   *     appended.
   */
  public static void appendFilteredBaggageAttributes(
      ViewBuilder viewBuilder, Predicate<String> keyFilter) {
    addAttributesProcessor(viewBuilder, AttributesProcessor.appendBaggageByKeyName(keyFilter));
  }

  /**
   * Reflectively add an {@link AttributesProcessor} to the {@link ViewBuilder} which appends all
   * key-values from baggage to all measurements.
   *
   * <p>Note: This runs after all other attribute processing added so far.
   *
   * @param viewBuilder the builder
   */
  public static void appendAllBaggageAttributes(ViewBuilder viewBuilder) {
    appendFilteredBaggageAttributes(viewBuilder, StringPredicates.ALL);
  }

  private static void addAttributesProcessor(
      ViewBuilder viewBuilder, AttributesProcessor attributesProcessor) {
    try {
      Method method =
          ViewBuilder.class.getDeclaredMethod("addAttributesProcessor", AttributesProcessor.class);
      method.setAccessible(true);
      method.invoke(viewBuilder, attributesProcessor);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Error adding AttributesProcessor to ViewBuilder", e);
    }
  }

  /**
   * Reflectively set the {@code cardinalityLimit} on the {@link ViewBuilder}.
   *
   * @param viewBuilder the builder
   */
  public static void setCardinalityLimit(ViewBuilder viewBuilder, int cardinalityLimit) {
    try {
      Method method = ViewBuilder.class.getDeclaredMethod("setCardinalityLimit", int.class);
      method.setAccessible(true);
      method.invoke(viewBuilder, cardinalityLimit);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Error setting cardinalityLimit on ViewBuilder", e);
    }
  }

  /** Reflectively reset the {@link SdkMeterProvider}, clearing all registered instruments. */
  public static void resetForTest(SdkMeterProvider sdkMeterProvider) {
    try {
      Method method = SdkMeterProvider.class.getDeclaredMethod("resetForTest");
      method.setAccessible(true);
      method.invoke(sdkMeterProvider);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalStateException("Error calling resetForTest on SdkMeterProvider", e);
    }
  }
}
