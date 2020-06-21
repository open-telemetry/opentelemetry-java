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

import static io.opentelemetry.common.AttributeValue.stringAttributeValue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.resources.ResourceConstants;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeanstalkResource extends AwsResource {

  private static final Logger logger = Logger.getLogger(BeanstalkResource.class.getName());

  private static final String DEVELOPMENT_ID = "deployment_id";
  private static final String VERSION_LABEL = "version_label";
  private static final String ENVIRONMENT_NAME = "environment_name";
  private static final String BEANSTALK_CONF_PATH = "/var/elasticbeanstalk/xray/environment.conf";
  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  @Override
  Map<String, AttributeValue> createAttributes() {
    File configFile = new File(BEANSTALK_CONF_PATH);
    if (!configFile.exists()) {
      return ImmutableMap.of();
    }

    ImmutableMap.Builder<String, AttributeValue> resourceAttributes = ImmutableMap.builder();

    try (JsonParser parser = JSON_FACTORY.createParser(configFile)) {
      parser.nextToken();

      if (!parser.isExpectedStartObjectToken()) {
        throw new IOException("Invalid Beanstalk config:" + BEANSTALK_CONF_PATH);
      }

      while (parser.nextToken() != JsonToken.END_OBJECT) {
        String value = parser.nextTextValue();
        switch (parser.getCurrentName()) {
          case DEVELOPMENT_ID:
            value = Long.toString(parser.getLongValue());
            resourceAttributes.put(ResourceConstants.SERVICE_INSTANCE, stringAttributeValue(value));
            break;
          case VERSION_LABEL:
            resourceAttributes.put(ResourceConstants.SERVICE_VERSION, stringAttributeValue(value));
            break;
          case ENVIRONMENT_NAME:
            resourceAttributes.put(
                ResourceConstants.SERVICE_NAMESPACE, stringAttributeValue(value));
            break;
          default:
            parser.skipChildren();
        }
      }
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not parse Beanstalk config.", e);
      return ImmutableMap.of();
    }

    return resourceAttributes.build();
  }
}
