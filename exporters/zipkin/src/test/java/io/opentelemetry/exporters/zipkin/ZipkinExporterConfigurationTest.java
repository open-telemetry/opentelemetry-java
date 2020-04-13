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

/** Unit tests for {@link ZipkinExporterConfiguration}. */
@RunWith(MockitoJUnitRunner.class)
public class ZipkinExporterConfigurationTest {

  private static final String SERVICE = "service";
  private static final String END_POINT = "endpoint";

  @Mock private Sender mockSender;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void updateConfigs() {
    ZipkinExporterConfiguration configuration =
        ZipkinExporterConfiguration.builder()
            .setServiceName(SERVICE)
            .setSender(mockSender)
            .setV2Url(END_POINT)
            .setEncoder(SpanBytesEncoder.PROTO3)
            .build();
    assertThat(configuration.getServiceName()).isEqualTo(SERVICE);
    assertThat(configuration.getV2Url()).isEqualTo(END_POINT);
    assertThat(configuration.getSender()).isEqualTo(mockSender);
    assertThat(configuration.getEncoder()).isEqualTo(SpanBytesEncoder.PROTO3);
  }

  @Test
  public void needEitherUrlOrSender() {
    ZipkinExporterConfiguration.Builder builder =
        ZipkinExporterConfiguration.builder().setServiceName(SERVICE);
    thrown.expect(IllegalArgumentException.class);
    builder.build();
  }
}
