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

package io.opentelemetry.sdk.extensions.trace.aws.resource;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BeanstalkResourceTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testCreateAttributes() throws IOException {
    File file = tempFolder.newFile("beanstalk.config");
    String content =
        "{\"noise\": \"noise\", \"deployment_id\":4,\""
            + "version_label\":\"2\",\"environment_name\":\"HttpSubscriber-env\"}";
    Files.write(content.getBytes(Charsets.UTF_8), file);
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Attributes attributes = populator.createAttributes();
    assertThat(attributes)
        .isEqualTo(
            Attributes.of(
                ResourceConstants.SERVICE_INSTANCE, stringAttributeValue("4"),
                ResourceConstants.SERVICE_VERSION, stringAttributeValue("2"),
                ResourceConstants.SERVICE_NAMESPACE, stringAttributeValue("HttpSubscriber-env")));
  }

  @Test
  public void testConfigFileMissing() throws IOException {
    BeanstalkResource populator = new BeanstalkResource("a_file_never_existing");
    Attributes attributes = populator.createAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }

  @Test
  public void testBadConfigFile() throws IOException {
    File file = tempFolder.newFile("beanstalk.config");
    String content =
        "\"deployment_id\":4,\"version_label\":\"2\",\""
            + "environment_name\":\"HttpSubscriber-env\"}";
    Files.write(content.getBytes(Charsets.UTF_8), file);
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Attributes attributes = populator.createAttributes();
    assertThat(attributes.isEmpty()).isTrue();
  }
}
