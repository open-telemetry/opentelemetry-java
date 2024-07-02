/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Attributes;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class AttributesFactory
    implements Factory<Attributes, io.opentelemetry.api.common.Attributes> {

  private static final AttributesFactory INSTANCE = new AttributesFactory();

  private AttributesFactory() {}

  static AttributesFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public io.opentelemetry.api.common.Attributes create(
      @Nullable Attributes model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return io.opentelemetry.api.common.Attributes.empty();
    }

    AttributesBuilder builder = io.opentelemetry.api.common.Attributes.builder();

    String serviceName = model.getServiceName();
    if (serviceName != null) {
      builder.put(stringKey("service.name"), serviceName);
    }

    model
        .getAdditionalProperties()
        .forEach(
            (key, value) -> {
              if (value == null) {
                throw new StructuredConfigException(
                    "Error processing attribute with key \"" + key + "\": unexpected null value");
              }
              if (value instanceof String) {
                builder.put(key, (String) value);
                return;
              }
              if (value instanceof Integer) {
                builder.put(key, (int) value);
                return;
              }
              if (value instanceof Long) {
                builder.put(key, (long) value);
                return;
              }
              if (value instanceof Double) {
                builder.put(key, (double) value);
                return;
              }
              if (value instanceof Float) {
                builder.put(key, (float) value);
                return;
              }
              if (value instanceof Boolean) {
                builder.put(key, (boolean) value);
                return;
              }
              if (value instanceof List) {
                List<?> values = (List<?>) value;
                if (values.isEmpty()) {
                  return;
                }
                Object first = values.get(0);
                if (first instanceof String) {
                  checkAllEntriesOfType(key, values, String.class);
                  builder.put(
                      AttributeKey.stringArrayKey(key),
                      values.stream().map(obj -> (String) obj).toArray(String[]::new));
                  return;
                }
                if (first instanceof Long) {
                  checkAllEntriesOfType(key, values, Long.class);
                  builder.put(
                      AttributeKey.longArrayKey(key),
                      values.stream().map(obj -> (long) obj).toArray(Long[]::new));
                  return;
                }
                if (first instanceof Integer) {
                  checkAllEntriesOfType(key, values, Integer.class);
                  builder.put(
                      AttributeKey.longArrayKey(key),
                      values.stream().map(obj -> Long.valueOf((int) obj)).toArray(Long[]::new));
                  return;
                }
                if (first instanceof Double) {
                  checkAllEntriesOfType(key, values, Double.class);
                  builder.put(
                      AttributeKey.doubleArrayKey(key),
                      values.stream().map(obj -> (double) obj).toArray(Double[]::new));
                  return;
                }
                if (first instanceof Float) {
                  checkAllEntriesOfType(key, values, Float.class);
                  builder.put(
                      AttributeKey.doubleArrayKey(key),
                      values.stream()
                          .map(obj -> Double.valueOf((float) obj))
                          .toArray(Double[]::new));
                  return;
                }
                if (first instanceof Boolean) {
                  checkAllEntriesOfType(key, values, Boolean.class);
                  builder.put(
                      AttributeKey.booleanArrayKey(key),
                      values.stream().map(obj -> (Boolean) obj).toArray(Boolean[]::new));
                  return;
                }
              }
              throw new StructuredConfigException(
                  "Error processing attribute with key \""
                      + key
                      + "\": unrecognized value type "
                      + value.getClass().getName());
            });

    return builder.build();
  }

  private static void checkAllEntriesOfType(String key, List<?> values, Class<?> expectedType) {
    values.forEach(
        value -> {
          if (value == null) {
            throw new StructuredConfigException(
                "Error processing attribute with key \""
                    + key
                    + "\": unexpected null element in value");
          }
          if (!expectedType.isAssignableFrom(value.getClass())) {
            throw new StructuredConfigException(
                "Error processing attribute with key \""
                    + key
                    + "\": expected value entries to be of type "
                    + expectedType
                    + " but found entry with type "
                    + value.getClass());
          }
        });
  }
}
