/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.zipkin;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

/** Unit tests for {@link ZipkinExporterConfiguration}. */
@RunWith(MockitoJUnitRunner.class)
public class ZipkinExporterConfigurationTest {

  @Mock private Sender mockSender;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void verifyOptionsAreApplied() {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder()
            .setSender(mockSender)
            .setEncoder(SpanBytesEncoder.PROTO3)
            .build();
    assertThat(configuration.getSender()).isEqualTo(mockSender);
    assertThat(configuration.getEncoder()).isEqualTo(SpanBytesEncoder.PROTO3);
  }

  @Test
  public void needToSpecifySender() {
    ZipkinExporterConfiguration.Builder builder = ZipkinExporterConfiguration.builder();
    thrown.expect(IllegalStateException.class);
    builder.build();
  }

  @Test
  public void senderIsEnough() {
    ZipkinExporterConfiguration.Builder builder =
        ZipkinExporterConfiguration.builder().setSender(mockSender);
    builder.build();
  }

  @Test
  public void urlIsEnough() {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.create("https://myzipkin.endpoint");
    assertThat(configuration).isNotNull();
    assertThat(configuration.getSender()).isInstanceOf(URLConnectionSender.class);
    assertThat(configuration.getEncoder()).isEqualTo(SpanBytesEncoder.JSON_V2);
  }
}
