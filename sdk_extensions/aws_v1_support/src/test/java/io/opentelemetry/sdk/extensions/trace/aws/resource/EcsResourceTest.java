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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.extensions.trace.aws.resource.utils.DockerUtil;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EcsResource.class)
public class EcsResourceTest {

  @Test
  public void testNotEcs() {
    EcsResource populator = new EcsResource(DockerUtil.getInstance());
    assertEquals(0, populator.createAttributes().size());
  }

  @Test
  public void testCreateAttributes() throws UnknownHostException {
    mockStatic(System.class);
    when(System.getenv("ECS_CONTAINER_METADATA_URI_V4")).thenReturn("ecs_metadata_uri");

    DockerUtil mockDockerUtil = mock(DockerUtil.class);
    when(mockDockerUtil.getContainerId()).thenReturn("0123456789");

    EcsResource populator = new EcsResource(mockDockerUtil);
    Map<String, AttributeValue> metadata = populator.createAttributes();
    assertEquals(2, metadata.size());
    assertEquals(
        InetAddress.getLocalHost().getHostName(),
        metadata.get(ResourceConstants.CONTAINER_NAME).getStringValue());
    assertEquals("0123456789", metadata.get("container.id").getStringValue());
  }
}
