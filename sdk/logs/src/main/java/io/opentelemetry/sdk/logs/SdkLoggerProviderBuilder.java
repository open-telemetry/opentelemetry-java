/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Builder class for {@link SdkLoggerProvider} instances.
 *
 * @since 1.27.0
 */
public final class SdkLoggerProviderBuilder {

  private final List<LogRecordProcessor> logRecordProcessors = new ArrayList<>();
  @Nullable private Supplier<Resource> resourceSupplier = null;
  private Resource resource = Resource.getDefault();
  private Supplier<LogLimits> logLimitsSupplier = LogLimits::getDefault;
  private Clock clock = Clock.getDefault();
  private ScopeConfiguratorBuilder<LoggerConfig> loggerConfiguratorBuilder =
      LoggerConfig.configuratorBuilder();
  private ExceptionAttributeResolver exceptionAttributeResolver =
      ExceptionAttributeResolver.getDefault();

  SdkLoggerProviderBuilder() {}

  /**
   * Assign a {@link Resource} to be attached to all {@link LogRecordData} created by {@link
   * Logger}s obtained from the {@link SdkLoggerProvider}.
   *
   * @param resource the resource
   * @return this
   */
  public SdkLoggerProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Merge a {@link Resource} with the current.
   *
   * @param resource {@link Resource} to merge with current.
   * @since 1.29.0
   */
  public SdkLoggerProviderBuilder addResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = this.resource.merge(resource);
    return this;
  }

  /**
   * Registers a supplier of {@link Resource}.
   *
   * <p>This will override any {@link #addResource(Resource)} or {@link #setResource(Resource)}
   * calls with the current supplier.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkLoggerProviderUtil#setResourceSupplier(SdkLoggerProviderBuilder, Supplier)}.
   *
   * @param supplier The supplier of {@link Resource}.
   * @since 1.X.0
   */
  SdkLoggerProviderBuilder setResourceSupplier(Supplier<Resource> supplier) {
    this.resourceSupplier = supplier;
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link LogLimits}. {@link LogLimits} will be retrieved each time a
   * {@link Logger#logRecordBuilder()} is called.
   *
   * <p>The {@code logLimitsSupplier} must be thread-safe and return immediately (no remote calls,
   * as contention free as possible).
   *
   * @param logLimitsSupplier the supplier that will be used to retrieve the {@link LogLimits} for
   *     every {@link LogRecordBuilder}.
   * @return this
   */
  public SdkLoggerProviderBuilder setLogLimits(Supplier<LogLimits> logLimitsSupplier) {
    requireNonNull(logLimitsSupplier, "logLimitsSupplier");
    this.logLimitsSupplier = logLimitsSupplier;
    return this;
  }

  /**
   * Add a log processor. {@link LogRecordProcessor#onEmit(Context, ReadWriteLogRecord)} will be
   * called each time a log is emitted by {@link Logger} instances obtained from the {@link
   * SdkLoggerProvider}.
   *
   * @param processor the log processor
   * @return this
   */
  public SdkLoggerProviderBuilder addLogRecordProcessor(LogRecordProcessor processor) {
    requireNonNull(processor, "processor");
    logRecordProcessors.add(processor);
    return this;
  }

  /**
   * Add a log processor as first. {@link LogRecordProcessor#onEmit(Context, ReadWriteLogRecord)}
   * will be called each time a log is emitted by {@link Logger} instances obtained from the {@link
   * SdkLoggerProvider}.
   *
   * <p>Compared to {@link SdkLoggerProviderBuilder#addLogRecordProcessor(LogRecordProcessor)}, this
   * method adds the processor to the beginning of the processor pipeline. So the {@code processor}
   * given will be executed before than the other processors already added.
   *
   * @param processor the log processor
   * @return this
   * @since 1.50.0
   */
  public SdkLoggerProviderBuilder addLogRecordProcessorFirst(LogRecordProcessor processor) {
    requireNonNull(processor, "processor");
    logRecordProcessors.add(0, processor);
    return this;
  }

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkLoggerProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Set the logger configurator, which computes {@link LoggerConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)}.
   *
   * <p>Overrides any matchers added via {@link #addLoggerConfiguratorCondition(Predicate,
   * LoggerConfig)}.
   *
   * @see LoggerConfig#configuratorBuilder()
   */
  SdkLoggerProviderBuilder setLoggerConfigurator(
      ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    this.loggerConfiguratorBuilder = loggerConfigurator.toBuilder();
    return this;
  }

  /**
   * Adds a condition to the logger configurator, which computes {@link LoggerConfig} for each
   * {@link InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkLoggerProviderUtil#addLoggerConfiguratorCondition(SdkLoggerProviderBuilder, Predicate,
   * LoggerConfig)}.
   *
   * <p>Applies after any previously added conditions.
   *
   * <p>If {@link #setLoggerConfigurator(ScopeConfigurator)} was previously called, this condition
   * will only be applied if the {@link ScopeConfigurator#apply(Object)} returns null for the
   * matched {@link InstrumentationScopeInfo}(s).
   *
   * @see ScopeConfiguratorBuilder#nameEquals(String)
   * @see ScopeConfiguratorBuilder#nameMatchesGlob(String)
   */
  SdkLoggerProviderBuilder addLoggerConfiguratorCondition(
      Predicate<InstrumentationScopeInfo> scopeMatcher, LoggerConfig loggerConfig) {
    this.loggerConfiguratorBuilder.addCondition(scopeMatcher, loggerConfig);
    return this;
  }

  /**
   * Set the exception attribute resolver, which resolves {@code exception.*} attributes an
   * exception is set on a log.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * SdkLoggerProviderUtil#setExceptionAttributeResolver(SdkLoggerProviderBuilder,
   * ExceptionAttributeResolver)}.
   */
  SdkLoggerProviderBuilder setExceptionAttributeResolver(
      ExceptionAttributeResolver exceptionAttributeResolver) {
    requireNonNull(exceptionAttributeResolver, "exceptionAttributeResolver");
    this.exceptionAttributeResolver = exceptionAttributeResolver;
    return this;
  }

  /**
   * Create a {@link SdkLoggerProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public SdkLoggerProvider build() {
    Supplier<Resource> resolvedSupplier = () -> this.resource;
    if (resourceSupplier != null) {
      resolvedSupplier = this.resourceSupplier;
    }
    return new SdkLoggerProvider(
        resolvedSupplier,
        logLimitsSupplier,
        logRecordProcessors,
        clock,
        loggerConfiguratorBuilder.build(),
        exceptionAttributeResolver);
  }
}
