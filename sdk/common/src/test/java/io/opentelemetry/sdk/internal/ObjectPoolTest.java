/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectPoolTest {
  private ObjectPool<StringBuilder> objectPool;

  @BeforeEach
  void setUp() {
    Supplier<StringBuilder> supplier = StringBuilder::new;
    objectPool = new ObjectPool<>(supplier);
  }

  @Test
  void testBorrowObjectWhenPoolIsEmpty() {
    StringBuilder result = objectPool.borrowObject();
    assertThat(result.toString()).isEmpty();
  }

  @Test
  void testReturnAndBorrowMultipleObjects() {
    // Borrow three objects
    StringBuilder borrowed1 = objectPool.borrowObject();
    StringBuilder borrowed2 = objectPool.borrowObject();
    StringBuilder borrowed3 = objectPool.borrowObject();

    // Modify and return the borrowed objects
    borrowed1.append("pooledObject1");
    objectPool.returnObject(borrowed1);
    borrowed2.append("pooledObject2");
    objectPool.returnObject(borrowed2);
    borrowed3.append("pooledObject3");
    objectPool.returnObject(borrowed3);

    // Borrow three objects, which should be the same ones we just returned
    StringBuilder result1 = objectPool.borrowObject();
    StringBuilder result2 = objectPool.borrowObject();
    StringBuilder result3 = objectPool.borrowObject();

    // Verify the results using AssertJ assertions and reference comparison
    List<StringBuilder> originalObjects = Arrays.asList(borrowed1, borrowed2, borrowed3);
    List<StringBuilder> borrowedObjects = Arrays.asList(result1, result2, result3);

    assertThat(originalObjects).hasSize(3);
    assertThat(borrowedObjects).hasSize(3);

    for (StringBuilder original : originalObjects) {
      assertThat(borrowedObjects).anySatisfy(borrowed -> assertThat(borrowed).isSameAs(original));
    }
  }
}
