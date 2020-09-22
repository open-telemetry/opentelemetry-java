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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EksResource extends ResourceProvider {

  private static final Logger logger = Logger.getLogger(EksResource.class.getName());

  private final DockerHelper dockerHelper;
  private final EksHelper eksHelper;

  /**
   * Returns a {@link EksResource} which attempts to compute information about the aws Eks
   * environment if available.
   */
  public EksResource() {
    this(new DockerHelper(), new EksHelper());
  }

  @VisibleForTesting
  EksResource(DockerHelper dockerHelper, EksHelper eksHelper) {
    this.dockerHelper = dockerHelper;
    this.eksHelper = eksHelper;
  }

  @Override
  protected Attributes getAttributes() {
    if (!eksHelper.isEks()) {
      return Attributes.empty();
    }

    logger.log(Level.INFO, "Retrieving aws eks metadata.");
    Attributes.Builder attrBuilders = Attributes.newBuilder();

    String clusterName = eksHelper.getClusterName();
    if (!Strings.isNullOrEmpty(clusterName)) {
      attrBuilders.setAttribute(ResourceAttributes.K8S_CLUSTER, clusterName);
    }

    String containerId = dockerHelper.getContainerId();
    if (!Strings.isNullOrEmpty(containerId)) {
      attrBuilders.setAttribute(ResourceAttributes.CONTAINER_ID, containerId);
    }

    return attrBuilders.build();
  }
}
