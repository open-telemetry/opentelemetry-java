/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;

import com.linecorp.armeria.common.AggregatedHttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class Ec2ResourceTest {

  // From https://docs.amazonaws.cn/en_us/AWSEC2/latest/UserGuide/instance-identity-documents.html
  private static final String IDENTITY_DOCUMENT =
      "{\n"
          + "    \"devpayProductCodes\" : null,\n"
          + "    \"marketplaceProductCodes\" : [ \"1abc2defghijklm3nopqrs4tu\" ], \n"
          + "    \"availabilityZone\" : \"us-west-2b\",\n"
          + "    \"privateIp\" : \"10.158.112.84\",\n"
          + "    \"version\" : \"2017-09-30\",\n"
          + "    \"instanceId\" : \"i-1234567890abcdef0\",\n"
          + "    \"billingProducts\" : null,\n"
          + "    \"instanceType\" : \"t2.micro\",\n"
          + "    \"accountId\" : \"123456789012\",\n"
          + "    \"imageId\" : \"ami-5fb8c835\",\n"
          + "    \"pendingTime\" : \"2016-11-19T16:32:11Z\",\n"
          + "    \"architecture\" : \"x86_64\",\n"
          + "    \"kernelId\" : null,\n"
          + "    \"ramdiskId\" : null,\n"
          + "    \"region\" : \"us-west-2\"\n"
          + "}";

  @RegisterExtension public static MockWebServerExtension server = new MockWebServerExtension();

  @Test
  void imdsv2() {
    server.enqueue(HttpResponse.of("token"));
    server.enqueue(HttpResponse.of(MediaType.JSON_UTF_8, IDENTITY_DOCUMENT));
    server.enqueue(HttpResponse.of("ec2-1-2-3-4"));

    Attributes attributes =
        Ec2Resource.buildResource("localhost:" + server.httpPort()).getAttributes();
    AttributesBuilder expectedAttrBuilders = Attributes.builder();

    expectedAttrBuilders.put(ResourceAttributes.CLOUD_PROVIDER, "aws");
    expectedAttrBuilders.put(ResourceAttributes.HOST_ID, "i-1234567890abcdef0");
    expectedAttrBuilders.put(ResourceAttributes.CLOUD_ZONE, "us-west-2b");
    expectedAttrBuilders.put(ResourceAttributes.HOST_TYPE, "t2.micro");
    expectedAttrBuilders.put(ResourceAttributes.HOST_IMAGE_ID, "ami-5fb8c835");
    expectedAttrBuilders.put(ResourceAttributes.CLOUD_ACCOUNT_ID, "123456789012");
    expectedAttrBuilders.put(ResourceAttributes.CLOUD_REGION, "us-west-2");
    expectedAttrBuilders.put(ResourceAttributes.HOST_NAME, "ec2-1-2-3-4");
    assertThat(attributes).isEqualTo(expectedAttrBuilders.build());

    AggregatedHttpRequest request1 = server.takeRequest().request();
    assertThat(request1.path()).isEqualTo("/latest/api/token");
    assertThat(request1.headers().get("X-aws-ec2-metadata-token-ttl-seconds")).isEqualTo("60");

    AggregatedHttpRequest request2 = server.takeRequest().request();
    assertThat(request2.path()).isEqualTo("/latest/dynamic/instance-identity/document");
    assertThat(request2.headers().get("X-aws-ec2-metadata-token")).isEqualTo("token");

    AggregatedHttpRequest request3 = server.takeRequest().request();
    assertThat(request3.path()).isEqualTo("/latest/meta-data/hostname");
    assertThat(request3.headers().get("X-aws-ec2-metadata-token")).isEqualTo("token");
  }

  @Test
  void imdsv1() {
    server.enqueue(HttpResponse.of(HttpStatus.NOT_FOUND));
    server.enqueue(HttpResponse.of(MediaType.JSON_UTF_8, IDENTITY_DOCUMENT));
    server.enqueue(HttpResponse.of("ec2-1-2-3-4"));

    Attributes attributes =
        Ec2Resource.buildResource("localhost:" + server.httpPort()).getAttributes();

    AttributesBuilder expectedAttrBuilders =
        Attributes.builder()
            .put(ResourceAttributes.CLOUD_PROVIDER, "aws")
            .put(ResourceAttributes.HOST_ID, "i-1234567890abcdef0")
            .put(ResourceAttributes.CLOUD_ZONE, "us-west-2b")
            .put(ResourceAttributes.HOST_TYPE, "t2.micro")
            .put(ResourceAttributes.HOST_IMAGE_ID, "ami-5fb8c835")
            .put(ResourceAttributes.CLOUD_ACCOUNT_ID, "123456789012")
            .put(ResourceAttributes.CLOUD_REGION, "us-west-2")
            .put(ResourceAttributes.HOST_NAME, "ec2-1-2-3-4");
    assertThat(attributes).isEqualTo(expectedAttrBuilders.build());

    AggregatedHttpRequest request1 = server.takeRequest().request();
    assertThat(request1.path()).isEqualTo("/latest/api/token");
    assertThat(request1.headers().get("X-aws-ec2-metadata-token-ttl-seconds")).isEqualTo("60");

    AggregatedHttpRequest request2 = server.takeRequest().request();
    assertThat(request2.path()).isEqualTo("/latest/dynamic/instance-identity/document");
    assertThat(request2.headers().get("X-aws-ec2-metadata-token")).isNull();
  }

  @Test
  void badJson() {
    server.enqueue(HttpResponse.of(HttpStatus.NOT_FOUND));
    server.enqueue(HttpResponse.of(MediaType.JSON_UTF_8, "I'm not JSON"));

    Attributes attributes =
        Ec2Resource.buildResource("localhost:" + server.httpPort()).getAttributes();
    assertThat(attributes.isEmpty()).isTrue();

    AggregatedHttpRequest request1 = server.takeRequest().request();
    assertThat(request1.path()).isEqualTo("/latest/api/token");
    assertThat(request1.headers().get("X-aws-ec2-metadata-token-ttl-seconds")).isEqualTo("60");

    AggregatedHttpRequest request2 = server.takeRequest().request();
    assertThat(request2.path()).isEqualTo("/latest/dynamic/instance-identity/document");
    assertThat(request2.headers().get("X-aws-ec2-metadata-token")).isNull();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(Ec2ResourceProvider.class::isInstance);
  }
}
