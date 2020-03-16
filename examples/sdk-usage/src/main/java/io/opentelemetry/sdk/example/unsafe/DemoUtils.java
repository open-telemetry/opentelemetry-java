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

package io.opentelemetry.sdk.example.unsafe;

import io.opentelemetry.sdk.trace.MultiSpanProcessor;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.TracerSdk;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * These methods use reflection to access private fields of the tracer. This is meant as
 * demonstration only. It is SEVERELY discouraged to access private fields since they are shared
 * among different threads.
 */
@SuppressWarnings("unchecked")
public class DemoUtils {

  public static void printProcessorList(TracerSdk tracer) throws Exception {
    Field sharedStateField = tracer.getClass().getDeclaredField("sharedState");
    sharedStateField.setAccessible(true);
    Object sharedState = sharedStateField.get(tracer);
    Method method = sharedState.getClass().getDeclaredMethod("getActiveSpanProcessor");
    method.setAccessible(true);
    Object multiSpanProcessor = method.invoke(sharedState);
    Field spanListField = multiSpanProcessor.getClass().getDeclaredField("spanProcessorsAll");
    spanListField.setAccessible(true);
    List<SpanProcessor> spanProcessors = (List) spanListField.get(multiSpanProcessor);
    System.out.println(spanProcessors.size() + " active span processors:");
    printProcessorList(spanProcessors, 1);
  }

  static void printProcessorList(List<SpanProcessor> spanProcessors, int tab) throws Exception {
    String tabs = new String(new char[tab]).replace("\0", "\t");
    for (SpanProcessor spanProcessor : spanProcessors) {
      System.out.print(tabs + "- ");
      System.out.println(spanProcessor.getClass().getName());
      if (spanProcessor instanceof MultiSpanProcessor) {
        Field listProcessors = spanProcessor.getClass().getDeclaredField("spanProcessorsAll");
        listProcessors.setAccessible(true);
        List<SpanProcessor> multiProcessorList = (List) listProcessors.get(spanProcessor);
        printProcessorList(multiProcessorList, tab + 1);
      }
    }
  }
}
