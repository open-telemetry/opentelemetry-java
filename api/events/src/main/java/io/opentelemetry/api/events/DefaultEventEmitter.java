/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;

class DefaultEventEmitter implements EventEmitter {

  private static final EventEmitter INSTANCE = new DefaultEventEmitter();

  private DefaultEventEmitter() {}

  static EventEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public void emit(String eventName, Attributes attributes) {}
}
