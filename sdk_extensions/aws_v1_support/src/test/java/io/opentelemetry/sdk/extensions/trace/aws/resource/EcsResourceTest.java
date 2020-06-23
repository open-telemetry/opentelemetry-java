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
import static org.mockito.Mockito.when;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class EcsResourceTest {

  @Rule public MockitoRule mocks = MockitoJUnit.rule();

  @Mock private DockerHelper mockDockerHelper;

  @Test
  public void testCreateAttributes() throws UnknownHostException {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");
    EcsResource populator = new EcsResource(mockDockerHelper, "ecs_metadata_v3_uri", null);
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertThat(metadata.get(ResourceConstants.CONTAINER_NAME).getStringValue())
        .isEqualTo(InetAddress.getLocalHost().getHostName());
    assertThat(metadata.get("container.id").getStringValue()).isEqualTo("0123456789A");
  }

  @Test
  public void testNotOnEcs() {
    when(mockDockerHelper.getContainerId()).thenReturn("0123456789A");
    EcsResource populator = new EcsResource(mockDockerHelper, "", null);
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertThat(metadata.size()).isEqualTo(0);
  }

  @Test
  public void testContainerIdMissing() {
    when(mockDockerHelper.getContainerId()).thenReturn("");
    EcsResource populator = new EcsResource(mockDockerHelper, null, "ecs_metadata_v4_uri");
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertThat(metadata.size()).isEqualTo(1);
    assertThat(metadata.get("container.id")).isNull();
  }
}
