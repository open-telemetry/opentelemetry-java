/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
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
public final class SdkLoggerProviderUtil {

  private SdkLoggerProviderUtil() {}

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkLoggerProvider}. */
  public static void setLoggerConfigurator(
      SdkLoggerProvider sdkLoggerProvider, ScopeConfigurator<LoggerConfig> scopeConfigurator) {
    try {
      Method method =
          SdkLoggerProvider.class.getDeclaredMethod(
              "setLoggerConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProvider, scopeConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setLoggerConfigurator on SdkLoggerProvider", e);
    }
  }

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkLoggerProviderBuilder}. */
  public static SdkLoggerProviderBuilder setLoggerConfigurator(
      SdkLoggerProviderBuilder sdkLoggerProviderBuilder,
      ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    try {
      Method method =
          SdkLoggerProviderBuilder.class.getDeclaredMethod(
              "setLoggerConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProviderBuilder, loggerConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setLoggerConfigurator on SdkLoggerProviderBuilder", e);
    }
    return sdkLoggerProviderBuilder;
  }

  /** Reflectively add a logger configurator condition to the {@link SdkLoggerProviderBuilder}. */
  public static SdkLoggerProviderBuilder addLoggerConfiguratorCondition(
      SdkLoggerProviderBuilder sdkLoggerProviderBuilder,
      Predicate<InstrumentationScopeInfo> scopeMatcher,
      LoggerConfig loggerConfig) {
    try {
      Method method =
          SdkLoggerProviderBuilder.class.getDeclaredMethod(
              "addLoggerConfiguratorCondition", Predicate.class, LoggerConfig.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProviderBuilder, scopeMatcher, loggerConfig);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling addLoggerConfiguratorCondition on SdkLoggerProviderBuilder", e);
    }
    return sdkLoggerProviderBuilder;
  }

  /** Reflectively set exception attribute resolver to the {@link SdkLoggerProviderBuilder}. */
  public static void setExceptionAttributeResolver(
      SdkLoggerProviderBuilder sdkLoggerProviderBuilder,
      ExceptionAttributeResolver exceptionAttributeResolver) {
    try {
      Method method =
          SdkLoggerProviderBuilder.class.getDeclaredMethod(
              "setExceptionAttributeResolver", ExceptionAttributeResolver.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProviderBuilder, exceptionAttributeResolver);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setExceptionAttributeResolver on SdkLoggerProviderBuilder", e);
    }
  }
}
