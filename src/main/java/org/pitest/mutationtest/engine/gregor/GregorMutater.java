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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.pitest.bytecode.FrameOptions;
import org.pitest.bytecode.NullVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ComputeClassWriter;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.MethodInfo;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Glob;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.pitest.functional.prelude.Prelude.and;

public class GregorMutater {

  private final Map<String, String>       computeCache   = new HashMap<>();
  private final Predicate<MethodInfo>     filter;
  private final byte[]      bytes;
  private final ClassByteArraySource      byteSource;
  private final List<MethodMutatorFactory> mutators;

  public GregorMutater(final byte[] byteSource,
                       final Collection<String> excludedMethod,
                       final Collection<MethodMutatorFactory> mutators) {
    this.filter = stringToMethodInfoPredicate(excludedMethod);
    this.mutators = orderAndDeDuplicate(mutators);
    this.bytes = byteSource;
    this.byteSource = new ClassPathByteArraySource();
  }

  public GregorMutater(final ClassByteArraySource byteSource,
                       final Predicate<MethodInfo> filter,
                       final Collection<MethodMutatorFactory> mutators) {
    this.filter = filter;
    this.mutators = orderAndDeDuplicate(mutators);
    this.bytes = null;
    this.byteSource = byteSource;
  }

  public List<MutationDetails> findMutations(String className) {

    final ClassContext context = new ClassContext();
    context.setTargetMutation(Optional.empty());
    Optional<byte[]> bytes = this.bytes == null ? GregorMutater.this.byteSource.getBytes(
            className.replace(".", "/")) : Optional.of(this.bytes);

    return bytes.map(findMutations(context))
        .orElse(Collections.emptyList());

  }

  private Function<byte[], List<MutationDetails>> findMutations(
      final ClassContext context) {
    return bytes -> findMutationsForBytes(context, bytes);
  }

  private List<MutationDetails> findMutationsForBytes(
      final ClassContext context, final byte[] classToMutate) {

    final ClassReader first = new ClassReader(classToMutate);
    final NullVisitor nv = new NullVisitor();
    final MutatingClassVisitor mca = new MutatingClassVisitor(nv, context, filterMethods(), this.mutators);

    first.accept(mca, ClassReader.EXPAND_FRAMES);

    return new ArrayList<>(context.getCollectedMutations());
  }

  public byte[] getMutation(final MutationIdentifier id) {

    final ClassContext context = new ClassContext();
    context.setTargetMutation(Optional.ofNullable(id));

    final Optional<byte[]> bytes = Optional.ofNullable(this.bytes);

    final ClassReader reader = new ClassReader(bytes.get());
    final ClassWriter w = new ComputeClassWriter(this.bytes,
            this.computeCache, FrameOptions.pickFlags(bytes.get()));
    final MutatingClassVisitor mca = new MutatingClassVisitor(w, context,
            filterMethods(), FCollection.filter(this.mutators,
            isMutatorFor(id)));
    reader.accept(mca, ClassReader.EXPAND_FRAMES);

    return w.toByteArray();

  }

  private static Predicate<MethodMutatorFactory> isMutatorFor(
      final MutationIdentifier id) {
    return a -> id.getMutator().equals(a.getGloballyUniqueId());
  }

  private Predicate<MethodInfo> filterMethods() {
    return and(i->true, filterSyntheticMethods(),
            isGeneratedEnumMethod().negate(), isGroovyClass().negate());
  }

  private static Predicate<MethodInfo> isGroovyClass() {
    return MethodInfo::isInGroovyClass;
  }

  private static Predicate<MethodInfo> filterSyntheticMethods() {
    return a -> !a.isSynthetic() || a.getName().startsWith("lambda$");
  }

  private static Predicate<MethodInfo> isGeneratedEnumMethod() {
    return MethodInfo::isGeneratedEnumMethod;
  }

  private List<MethodMutatorFactory> orderAndDeDuplicate(Collection<MethodMutatorFactory> mutators) {
    // deduplication is based on object identity, so dubious that this adds any value
    // however left in place for now to replicate HashSet behaviour
    return mutators.stream()
            .distinct()
            .sorted(Comparator.comparing(MethodMutatorFactory::getGloballyUniqueId))
            .collect(Collectors.toList());
  }

  private Predicate<MethodInfo> stringToMethodInfoPredicate(
          final Collection<String> excludedMethods) {
    final Predicate<String> excluded = Prelude.or(Glob.toGlobPredicates(excludedMethods));
    return a -> excluded.test(a.getName());
  }

}
