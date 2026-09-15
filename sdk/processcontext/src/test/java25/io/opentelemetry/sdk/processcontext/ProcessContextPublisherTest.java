/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessContextPublisherTest {

  @Test
  void lifecycle() throws Throwable {

    assertThat(containsMappingWith("OTEL_CTX")).isFalse();

    ProcessContextData processContextData =
        ProcessContextData.create(
            Resource.create(Attributes.of(AttributeKey.stringKey("foo1"), "bar1")),
            Attributes.of(AttributeKey.stringKey("foo2"), "bar2"));

    ProcessContextPublisher processContextPublisher = new PanamaProcessContextPublisher();

    processContextPublisher.publish(processContextData, TestClock.create());
    assertThat(containsMappingWith("OTEL_CTX")).isTrue();

    processContextPublisher.close();
    assertThat(containsMappingWith("OTEL_CTX")).isFalse();
  }

  private static boolean containsMappingWith(String value) throws IOException {

    String pid = ManagementFactory.getRuntimeMXBean().getName();
    pid = pid.substring(0, pid.indexOf('@')); // expect format e.g. pidnumber@thing"

    List<String> lines = Files.readAllLines(Path.of("/proc/" + pid + "/maps"));
    for (String line : lines) {
      if (line.contains(value)) {
        return true;
      }
    }
    return false;
  }
}
