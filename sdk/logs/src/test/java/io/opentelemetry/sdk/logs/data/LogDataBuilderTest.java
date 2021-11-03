/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class LogDataBuilderTest {

  @Test
  void canSetClock() {
    Resource resource = Resource.getDefault();
    InstrumentationLibraryInfo libraryInfo = InstrumentationLibraryInfo.empty();
    LogDataBuilder builder = new LogDataBuilder(resource, libraryInfo);

    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(12L);

    builder.setClock(clock);
    LogData result = builder.build();
    assertEquals(12L, result.getEpochNanos());
  }
}
