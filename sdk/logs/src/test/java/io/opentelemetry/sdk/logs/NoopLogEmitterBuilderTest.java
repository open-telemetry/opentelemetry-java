/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class NoopLogEmitterBuilderTest {

  @Test
  void buildAndEmit() {
    NoopLogEmitterBuilder.getInstance()
        .setSchemaUrl("http://endpoint")
        .setInstrumentationVersion("1.0.0")
        .build()
        .logBuilder()
        .setAttributes(Attributes.empty())
        .setEpoch(Instant.now())
        .setEpoch(100, TimeUnit.SECONDS)
        .setSeverity(Severity.DEBUG)
        .setSeverityText("debug")
        .setContext(Context.root())
        .setBody("body")
        .emit();
    ;
  }
}
