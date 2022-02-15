/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.annotation.Nullable;

final class ExceptionEventUtil {
  private static final String CLASS_NAME =
      "io.opentelemetry.sdk.extension.incubator.trace.data.ExceptionEventData";
  private static final String METHOD_NAME = "create";
  private static final Class<?>[] PARAMETER_TYPES = {long.class, Throwable.class, Attributes.class};

  private static final ExceptionEventFactory factory = initializeEventFactory();

  static EventData create(Throwable exception, Attributes additionalAttributes, long epochNanos) {
    return factory.create(exception, additionalAttributes, epochNanos);
  }

  private static ExceptionEventFactory initializeEventFactory() {
    Method createMethod = findExceptionEventCreateMethod();
    if (createMethod != null) {
      return (exception, attributes, epochNanos) -> {
        try {
          return (EventData) createMethod.invoke(null, epochNanos, exception, attributes);
        } catch (Exception e) {
          return createEventData(exception, attributes, epochNanos);
        }
      };
    } else {
      return ExceptionEventUtil::createEventData;
    }
  }

  @Nullable
  private static Method findExceptionEventCreateMethod() {
    try {
      Class<?> clazz = Class.forName(CLASS_NAME);
      Method method = clazz.getMethod(METHOD_NAME, PARAMETER_TYPES);
      int modifiers = method.getModifiers();
      if (Modifier.isPublic(modifiers)
          && Modifier.isStatic(modifiers)
          && EventData.class.isAssignableFrom(method.getReturnType())) {

        method.setAccessible(true);
        return method;
      }
      return null;
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return null;
    }
  }

  private static EventData createEventData(
      Throwable exception, Attributes additionalAttributes, long epochNanos) {
    AttributesBuilder attributes = Attributes.builder();
    attributes.put(SemanticAttributes.EXCEPTION_TYPE, exception.getClass().getCanonicalName());
    if (exception.getMessage() != null) {
      attributes.put(SemanticAttributes.EXCEPTION_MESSAGE, exception.getMessage());
    }
    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    attributes.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
    attributes.putAll(additionalAttributes);

    return EventData.create(
        epochNanos, SemanticAttributes.EXCEPTION_EVENT_NAME, attributes.build());
  }

  private ExceptionEventUtil() {}

  @FunctionalInterface
  private interface ExceptionEventFactory {
    EventData create(Throwable exception, Attributes additionalAttributes, long epochNanos);
  }
}
