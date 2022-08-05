/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MutatorTestBase;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class ReturnValsMutatorTest extends MutatorTestBase {

  @Before
  public void setupEngineToMutateOnlyReturnVals() {
    List<String> mutators = new ArrayList<>();
    mutators.add("RETURN_VALS");
    createTesteeWith(getByteArrayFrom(), Collections.emptyList(), Mutator.fromStrings(mutators));
  }

  private byte[] getByteArrayFrom() {
    ClassReader classReader = null;
    try {
      classReader = new ClassReader(AReturn.class.getName());
    } catch (IOException e) {
      e.printStackTrace();
    }
    ClassWriter writer = new ClassWriter(classReader, ClassReader.EXPAND_FRAMES);

    return writer.toByteArray();
  }

  private static class IReturn implements Callable<String> {
    private final int value;

    public IReturn(final int value) {
      this.value = value;
    }

    public int mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateIReturnsOf0To1() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(IReturn.class);
    Assertions.assertThat(actual).size().isEqualTo(2);
  }

  private static class AReturn implements Callable<String> {

    private final Object value;

    public AReturn(final Object value) {
      this.value = value;
    }

    public Object mutable() {
      return this.value;
    }

    @Override
    public String call() throws Exception {
      return "" + mutable();
    }

  }

  @Test
  public void shouldMutateReturnsOfNonNullObjectsToNull() throws Exception {
    final Collection<MutationDetails> actual = findMutationsFor(AReturn.class);
    Assertions.assertThat(actual).size().isNotEqualTo(0);
  }
}
