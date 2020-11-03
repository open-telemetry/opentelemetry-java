/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
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
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Attributes attributes = populator.getAttributes();
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceAttributes.CLOUD_PROVIDER, "aws",
                ResourceAttributes.SERVICE_INSTANCE, "4",
                ResourceAttributes.SERVICE_VERSION, "2",
                ResourceAttributes.SERVICE_NAMESPACE, "HttpSubscriber-env"));
  }

  @Test
  void testConfigFileMissing() {
    BeanstalkResource populator = new BeanstalkResource("a_file_never_existing");
    Attributes attributes = populator.getAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void testBadConfigFile(@TempDir File tempFolder) throws IOException {
    File file = new File(tempFolder, "beanstalk.config");
    String content =
        "\"deployment_id\":4,\"version_label\":\"2\",\""
            + "environment_name\":\"HttpSubscriber-env\"}";
    Files.write(content.getBytes(Charsets.UTF_8), file);
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Attributes attributes = populator.getAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  void inServiceLoader() {
    // No practical way to test the attributes themselves so at least check the service loader picks
    // it up.
    assertThat(ServiceLoader.load(ResourceProvider.class))
        .anyMatch(BeanstalkResource.class::isInstance);
  }
}
