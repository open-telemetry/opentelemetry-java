/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.data.EventData;

public final class ExceptionEventData {

// Fields are private and final for immutability
  private final String exceptionType;
  private final String message;,
  private final long timestamp;
  private final List<String> stackTrace; // Mutable object (List)


// Constructor with input validation (fail-fast approach)
public ExceptionEventData(String exceptionType, String message, long timestamp, List<String> stackTrace) {
  // Validate input parameters to ensure stability

if (exceptionType == null || exceptionType.isEmpty()) {
  throw new IllegalArgumentException("Exception type cannot be null or empty");
}
if (message == null || message.isEmpty()) {
  throw new IllegalArgumentException("Message cannot be null or empty");
}
if (stackTrace == null || stackTrace.isEmpty()) {
  throw new IllegalArgumentException("Stack trace cannot be null or empty");
}

// Initialize the fields after validation
this.exceptionType = exceptionType;
this.message = message;
this.timestamp = timestamp;

// Defensive copy for mutable objects (List) to avoid external modification
this.stackTrace = new ImmutableList<>(stackTrace);
}

// Public getters for the fields
public String getExceptionType() {
  return exceptionType;
}
public String getMessage() {
  return message;
}
public long getTimestamp() {
  return timestamp;
}

// Defensive copy for mutable objects to ensure immutability
public List<String> getStackTrace() {
  return Collections.unmodifiableList(stackTrace);
}

// Overriding equals() to ensure correct comparison of objects
@Override public boolean equals(Object o) {
  if (this == o) return true;
  if (o == null || getClass() != o.getClass()) return false;
  ExceptionEventData that = (ExceptionEventData) o;
  return timestamp == that.timestamp &&
  Objects.equals(exceptionType, that.exceptionType) &&
  Objects.equals(message, that.message) &&
  Objects.equals(stackTrace, that.stackTrace);
}

// Overriding hashCode() for correct behavior in hash-based collections
@Override public int hashCode() {
  return Objects.hash(exceptionType, message, timestamp, stackTrace);
}

// Overriding toString() for helpful debugging output
@Override public String toString() {
  return "ExceptionEventData{" + 
  "exceptionType='" 
  + exceptionType + '\'' + 
  ", message='" +
  message + '\'' +
  ", timestamp=" +
  timestamp + 
  ", stackTrace=" +
  stackTrace +
  '}';
}
}

