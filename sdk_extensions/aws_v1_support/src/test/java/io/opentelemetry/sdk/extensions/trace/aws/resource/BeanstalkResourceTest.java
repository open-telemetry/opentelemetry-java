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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BeanstalkResourceTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testCreateAttributes() throws IOException {
    File file = tempFolder.newFile("beanstalk.config");
    FileUtils.writeStringToFile(
        file,
        "{\"noise\": \"noise\", \"deployment_id\":4,\""
            + "version_label\":\"2\",\"environment_name\":\"HttpSubscriber-env\"}");
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Map<String, AttributeValue> metadata = populator.createAttributes();

    assertThat(metadata.size()).isEqualTo(3);
    assertThat(metadata.get(ResourceConstants.SERVICE_INSTANCE).getStringValue()).isEqualTo("4");
    assertThat(metadata.get(ResourceConstants.SERVICE_VERSION).getStringValue()).isEqualTo("2");
    assertThat(metadata.get(ResourceConstants.SERVICE_NAMESPACE).getStringValue())
        .isEqualTo("HttpSubscriber-env");
  }

  @Test
  public void testConfigFileMissing() throws IOException {
    BeanstalkResource populator = new BeanstalkResource("a_file_never_existing");
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertThat(metadata.size()).isEqualTo(0);
  }

  @Test
  public void testBadConfigFile() throws IOException {
    File file = tempFolder.newFile("beanstalk.config");
    FileUtils.writeStringToFile(
        file,
        "\"deployment_id\":4,\"version_label\":\"2\",\""
            + "environment_name\":\"HttpSubscriber-env\"}");
    BeanstalkResource populator = new BeanstalkResource(file.getPath());
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertThat(metadata.size()).isEqualTo(0);
  }
}
