/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws.resource;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BeanstalkResourceTest {

  @Test
  void testCreateAttributes(@TempDir File tempFolder) throws IOException {
    File file = new File(tempFolder, "beanstalk.config");
    String content =
        "{\"noise\": \"noise\", \"deployment_id\":4,\""
            + "version_label\":\"2\",\"environment_name\":\"HttpSubscriber-env\"}";
    Files.write(content.getBytes(Charsets.UTF_8), file);
    Resource resource = BeanstalkResource.buildResource(file.getPath());
    Attributes attributes = resource.getAttributes();
    assertThat(attributes)
        .containsOnly(
            entry(ResourceAttributes.CLOUD_PROVIDER, "aws"),
            entry(ResourceAttributes.CLOUD_PLATFORM, "aws_elastic_beanstalk"),
            entry(ResourceAttributes.SERVICE_INSTANCE_ID, "4"),
            entry(ResourceAttributes.SERVICE_VERSION, "2"),
            entry(ResourceAttributes.SERVICE_NAMESPACE, "HttpSubscriber-env"));
    assertThat(resource.getSchemaUrl()).isEqualTo(ResourceAttributes.SCHEMA_URL);
  }

  @Test
  void testConfigFileMissing() {
    Attributes attributes =
        BeanstalkResource.buildResource("a_file_never_existing").getAttributes();
    assertThat(attributes).isEmpty();
  }

  @Test
  void testBadConfigFile(@TempDir File tempFolder) throws IOException {
    File file = new File(tempFolder, "beanstalk.config");
    String content =
        "\"deployment_id\":4,\"version_label\":\"2\",\""
            + "environment_name\":\"HttpSubscriber-env\"}";
    Files.write(content.getBytes(Charsets.UTF_8), file);
    Attributes attributes = BeanstalkResource.buildResource(file.getPath()).getAttributes();
    assertThat(attributes).isEmpty();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(BeanstalkResourceProvider.class::isInstance);
  }
}
