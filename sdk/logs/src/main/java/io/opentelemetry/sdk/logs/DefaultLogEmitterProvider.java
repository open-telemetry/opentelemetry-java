/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

/** A {@link LogEmitterProvider} that does nothing. */
class DefaultLogEmitterProvider implements LogEmitterProvider {

  private static final DefaultLogEmitterProvider INSTANCE = new DefaultLogEmitterProvider();
  private static final LogEmitterBuilder BUILDER_INSTANCE = new NoopLogBuilder();

  public static LogEmitterProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public LogEmitterBuilder logEmitterBuilder(String instrumentationName) {
    return BUILDER_INSTANCE;
  }

  private DefaultLogEmitterProvider() {}

  private static class NoopLogBuilder implements LogEmitterBuilder {
    @Override
    public LogEmitterBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public LogEmitterBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public LogEmitter build() {
      return DefaultLogEmitter.getInstance();
    }
  }
}
