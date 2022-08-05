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
package org.pitest.mutationtest.engine.gregor;

import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.List;

public abstract class MutatorTestBase {

    protected GregorMutater engine;

    protected List<MutationDetails> findMutationsFor(
            final Class<?> clazz) {
        return this.engine.findMutations(clazz.getName());
    }

    protected void createTesteeWith(final byte[] source,
                                    final Collection<String> filter,
                                    final Collection<MethodMutatorFactory> mutators) {
        this.engine = new GregorMutater(new ClassPathByteArraySource(), i->true, mutators);
    }

    private static class MyClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] b) {
            return super.defineClass(name, b, 0, b.length);
        }
    }
}
