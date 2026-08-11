/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.processcontext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.processcontext.data.ProcessContextData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessContextPublisherTest {

  @Test
  void publicationTest() throws Throwable {

    assertFalse(containsMappingWith("OTEL_CTX"));

    ProcessContextData processContextData =
        ProcessContextData.create(
            Resource.create(Attributes.of(AttributeKey.stringKey("foo1"), "bar1")),
            Attributes.of(AttributeKey.stringKey("foo2"), "bar2"));

    ProcessContextPublisher processContextPublisher = new DefaultProcessContextPublisher();
    processContextPublisher.publish(processContextData);

    assertTrue(containsMappingWith("OTEL_CTX"));
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
