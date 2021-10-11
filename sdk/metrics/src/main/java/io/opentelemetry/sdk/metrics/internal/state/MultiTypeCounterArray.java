/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

/* Copyright 2021 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 * This file is part of the NrSketch project.
 */

package io.opentelemetry.sdk.metrics.internal.state;

/**
 * This class uses the smallest of byte, short, int, or long to hold the count.
 * It auto scales to the next larger type.
 */
class MultiTypeCounterArray {
  private byte bytesPerCounter;
  private byte[] byteArray;
  private short[] shortArray;
  private int[] intArray;
  private long[] longArray;

  public MultiTypeCounterArray(final int maxSize) {
    this(maxSize, (byte) Byte.BYTES); // Start from smallest type
  }

  public MultiTypeCounterArray(final int maxSize, final byte bytesPerCounter) {
    switch (bytesPerCounter) {
      case Byte.BYTES:
        this.bytesPerCounter = Byte.BYTES;
        byteArray = new byte[maxSize];
        return;
      case Short.BYTES:
        this.bytesPerCounter = Short.BYTES;
        shortArray = new short[maxSize];
        return;
      case Integer.BYTES:
        this.bytesPerCounter = Integer.BYTES;
        intArray = new int[maxSize];
        return;
      case Long.BYTES:
        this.bytesPerCounter = Long.BYTES;
        longArray = new long[maxSize];
        return;
      default:
        throw new RuntimeException("Unknown counter size " + bytesPerCounter);
    }
  }

  // For simplicity, only sequential scaling from one size to the next larger one is coded.
  // Size skipping scaling is achieved by increment() recursion.
  //
  public void increment(final int index, final long delta) {
    final long count;
    switch (bytesPerCounter) {
      case Byte.BYTES:
        count = byteArray[index] + delta;
        if (count > Byte.MAX_VALUE) {
          convertByteToShort();
          increment(index, delta);
          return;
        }
        byteArray[index] = (byte) count;
        return;
      case Short.BYTES:
        count = shortArray[index] + delta;
        if (count > Short.MAX_VALUE) {
          convertShortToInt();
          increment(index, delta);
          return;
        }
        shortArray[index] = (short) count;
        return;
      case Integer.BYTES:
        count = intArray[index] + delta;
        if (count > Integer.MAX_VALUE) {
          convertIntToLong();
          increment(index, delta);
          return;
        }
        intArray[index] = (int) count;
        return;
      case Long.BYTES:
        longArray[index] += delta;
        return;
      default:
        throw new RuntimeException("Unknown counter size " + bytesPerCounter);
    }
  }

  public long get(final int index) {
    switch (bytesPerCounter) {
      case Byte.BYTES:
        return byteArray[index];
      case Short.BYTES:
        return shortArray[index];
      case Integer.BYTES:
        return intArray[index];
      case Long.BYTES:
        return longArray[index];
      default:
        throw new RuntimeException("Unknown counter size " + bytesPerCounter);
    }
  }

  public byte getBytesPerCounter() {
    return bytesPerCounter;
  }

  public int getMaxSize() {
    switch (bytesPerCounter) {
      case Byte.BYTES:
        return byteArray.length;
      case Short.BYTES:
        return shortArray.length;
      case Integer.BYTES:
        return intArray.length;
      case Long.BYTES:
        return longArray.length;
      default:
        throw new RuntimeException("Unknown counter size " + bytesPerCounter);
    }
  }

  private void convertByteToShort() {
    shortArray = new short[byteArray.length];
    for (int i = 0; i < byteArray.length; i++) {
      shortArray[i] = byteArray[i];
    }
    byteArray = null;
    bytesPerCounter = Short.BYTES;
  }

  private void convertShortToInt() {
    intArray = new int[shortArray.length];
    for (int i = 0; i < shortArray.length; i++) {
      intArray[i] = shortArray[i];
    }
    shortArray = null;
    bytesPerCounter = Integer.BYTES;
  }

  private void convertIntToLong() {
    longArray = new long[intArray.length];
    for (int i = 0; i < intArray.length; i++) {
      longArray[i] = intArray[i];
    }
    intArray = null;
    bytesPerCounter = Long.BYTES;
  }
}
