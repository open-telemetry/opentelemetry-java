/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.sdk.internal.SemConvAttributes;
import org.junit.jupiter.api.Test;

public class ServerAttributesUtilTest {

  @Test
  public void invalidUrl() {
    assertThat(ServerAttributesUtil.extractServerAttributes("^")).isEmpty();
  }

  @Test
  public void emptyUrl() {
    assertThat(ServerAttributesUtil.extractServerAttributes("")).isEmpty();
  }

  @Test
  public void testHttps() {
    assertThat(ServerAttributesUtil.extractServerAttributes("https://example.com/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 443);

    assertThat(ServerAttributesUtil.extractServerAttributes("https://example.com:1234/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }

  @Test
  public void testHttp() {
    assertThat(ServerAttributesUtil.extractServerAttributes("http://example.com/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 80);

    assertThat(ServerAttributesUtil.extractServerAttributes("http://example.com:1234/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }

  @Test
  public void unknownScheme() {
    assertThat(ServerAttributesUtil.extractServerAttributes("custom://foo"))
        .hasSize(1)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "foo");

    assertThat(ServerAttributesUtil.extractServerAttributes("custom://foo:1234"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "foo")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }
}
