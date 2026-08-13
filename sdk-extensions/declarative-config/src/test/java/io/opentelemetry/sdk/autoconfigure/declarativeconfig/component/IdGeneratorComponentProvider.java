/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.trace.IdGenerator;

public class IdGeneratorComponentProvider implements ComponentProvider {
  @Override
  public Class<IdGenerator> getType() {
    return IdGenerator.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public IdGenerator create(DeclarativeConfigProperties config) {
    return TestIdGenerator.create();
  }

  public static class TestIdGenerator implements IdGenerator {

    private TestIdGenerator() {}

    public static TestIdGenerator create() {
      return new TestIdGenerator();
    }

    @Override
    public String generateSpanId() {
      return "00000000";
    }

    @Override
    public String generateTraceId() {
      return "0000000000000000";
    }

    @Override
    public String toString() {
      return "TestIdGenerator{}";
    }
  }
}
