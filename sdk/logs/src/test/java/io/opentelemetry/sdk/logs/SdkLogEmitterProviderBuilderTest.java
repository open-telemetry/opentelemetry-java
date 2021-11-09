/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SdkLogEmitterProviderBuilderTest {

  @Test
  void canSetClock() {
    Clock clock = mock(Clock.class);
    when(clock.now()).thenReturn(13L);

    List<LogData> seenLogs = new LinkedList<>();

    LogProcessor processor = seenLogs::add;

    SdkLogEmitterProviderBuilder builder =
        new SdkLogEmitterProviderBuilder()
            .setResource(Resource.getDefault())
            .addLogProcessor(processor)
            .setClock(clock);

    SdkLogEmitterProvider provider = builder.build();
    provider.logEmitterBuilder("inst").build().logBuilder().emit();
    assertThat(seenLogs.size()).isEqualTo(1);
    assertThat(seenLogs.get(0).getEpochNanos()).isEqualTo(13L);
  }
}
