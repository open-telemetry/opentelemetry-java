/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class OkHttpGrpcServiceBuilderTest {

  private static OkHttpGrpcServiceBuilder<
          SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler>
      exporterBuilder() {
    return new OkHttpGrpcServiceBuilder<>("some", "dpp", 10, URI.create("htt://localhost:8080"));
  }

  @Test
  @SuppressWarnings({"PreferJavaTimeOverload", "NullAway"})
  public void invalidConfig() {
    assertThatThrownBy(() -> exporterBuilder().setTimeout(-1, TimeUnit.MILLISECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("timeout must be non-negative");
    assertThatThrownBy(() -> exporterBuilder().setTimeout(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
    assertThatThrownBy(() -> exporterBuilder().setTimeout(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("timeout");

    assertThatThrownBy(() -> exporterBuilder().addRetryPolicy(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("retryPolicy");

    assertThatThrownBy(() -> exporterBuilder().addHeader(null, "val"))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("key");
    assertThatThrownBy(() -> exporterBuilder().addHeader("key", null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("val");

    assertThatThrownBy(() -> exporterBuilder().setTrustedCertificates(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("trustedCertificatesPem");

    assertThatThrownBy(() -> exporterBuilder().setEndpoint(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("endpoint");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("😺://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must be a URL: 😺://localhost");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: localhost");
    assertThatThrownBy(() -> exporterBuilder().setEndpoint("gopher://localhost"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid endpoint, must start with http:// or https://: gopher://localhost");

    assertThatThrownBy(() -> exporterBuilder().setCompression(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("compressionMethod");
    assertThatThrownBy(() -> exporterBuilder().setCompression("foo"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Unsupported compression method. Supported compression methods include: gzip, none.");
  }
}
