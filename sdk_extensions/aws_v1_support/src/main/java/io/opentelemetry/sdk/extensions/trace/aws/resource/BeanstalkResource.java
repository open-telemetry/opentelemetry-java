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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.resources.ResourceAttributes;
import io.opentelemetry.sdk.resources.ResourceProvider;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link ResourceProvider} which provides information about the current EC2 instance if running
 * on AWS Elastic Beanstalk.
 */
public class BeanstalkResource extends ResourceProvider {

  private static final Logger logger = Logger.getLogger(BeanstalkResource.class.getName());

  private static final String DEVELOPMENT_ID = "deployment_id";
  private static final String VERSION_LABEL = "version_label";
  private static final String ENVIRONMENT_NAME = "environment_name";
  private static final String BEANSTALK_CONF_PATH = "/var/elasticbeanstalk/xray/environment.conf";
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  private final String configPath;

  /**
   * Returns a {@link BeanstalkResource} which attempts to compute information about the Beanstalk
   * environment if available.
   */
  public BeanstalkResource() {
    this(BEANSTALK_CONF_PATH);
  }

  @VisibleForTesting
  BeanstalkResource(String configPath) {
    this.configPath = configPath;
  }

  @Override
  public Attributes getAttributes() {
    File configFile = new File(configPath);
    if (!configFile.exists()) {
      return Attributes.empty();
    }

    Attributes.Builder attrBuilders = Attributes.newBuilder();
    try (JsonParser parser = JSON_FACTORY.createParser(configFile)) {
      parser.nextToken();

      if (!parser.isExpectedStartObjectToken()) {
        logger.log(Level.WARNING, "Invalid Beanstalk config: ", configPath);
        return attrBuilders.build();
      }

      while (parser.nextToken() != JsonToken.END_OBJECT) {
        parser.nextValue();
        String value = parser.getText();
        switch (parser.getCurrentName()) {
          case DEVELOPMENT_ID:
            ResourceAttributes.SERVICE_INSTANCE.set(attrBuilders, value);
            break;
          case VERSION_LABEL:
            ResourceAttributes.SERVICE_VERSION.set(attrBuilders, value);
            break;
          case ENVIRONMENT_NAME:
            ResourceAttributes.SERVICE_NAMESPACE.set(attrBuilders, value);
            break;
          default:
            parser.skipChildren();
        }
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not parse Beanstalk config.", e);
      return Attributes.empty();
    }

    return attrBuilders.build();
  }
}
