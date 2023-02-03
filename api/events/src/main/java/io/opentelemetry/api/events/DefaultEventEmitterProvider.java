/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

class DefaultEventEmitterProvider implements EventEmitterProvider {

  private static final EventEmitterProvider INSTANCE = new DefaultEventEmitterProvider();
  private static final EventEmitterBuilder NOOP_EVENT_EMITTER_BUILDER =
      new NoopEventEmitterBuilder();

  private DefaultEventEmitterProvider() {}

  static EventEmitterProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName) {
    return NOOP_EVENT_EMITTER_BUILDER;
  }

  private static class NoopEventEmitterBuilder implements EventEmitterBuilder {

    @Override
    public EventEmitterBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public EventEmitterBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public EventEmitterBuilder setEventDomain(String eventDomain) {
      return this;
    }

    @Override
    public EventEmitter build() {
      return DefaultEventEmitter.getInstance();
    }
  }
}
