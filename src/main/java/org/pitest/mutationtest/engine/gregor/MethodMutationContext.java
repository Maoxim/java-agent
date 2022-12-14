package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.ClassInfo;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

import java.util.HashSet;
import java.util.Set;

public class MethodMutationContext implements MutationContext, InstructionCounter {

  private final ClassContext classContext;
  private final Location     location;

  private int                instructionIndex;

  private int                lastLineNumber;
  private final Set<String>  mutationFindingDisabledReasons = new HashSet<>();

  MethodMutationContext(final ClassContext classContext, final Location location) {
    this.classContext = classContext;
    this.location = location;
  }

  public MutationIdentifier registerMutation(
      final MethodMutatorFactory factory, final String description) {
    final MutationIdentifier newId = getNextMutationIdentifer(factory,
        this.classContext.getJavaClassName());
    final MutationDetails details = new MutationDetails(newId,
        this.classContext.getFileName(), description, this.lastLineNumber,
        this.classContext.getCurrentBlock());
    registerMutation(details);
    return newId;
  }

  private MutationIdentifier getNextMutationIdentifer(
      final MethodMutatorFactory factory, final String className) {
    return new MutationIdentifier(this.location, this.instructionIndex,
        factory.getGloballyUniqueId());
  }

  private void registerMutation(final MutationDetails details) {
    if (!isMutationFindingDisabled()) {
      this.classContext.addMutation(details);
    }
  }

  private boolean isMutationFindingDisabled() {
    return !this.mutationFindingDisabledReasons.isEmpty();
  }

  public void registerCurrentLine(final int line) {
    this.lastLineNumber = line;
  }

  public void registerNewBlock() {
    this.classContext.registerNewBlock();
  }

  public void registerNewMethodStart() {
    this.classContext.registerNewMethodStart();
  }

  public ClassInfo getClassInfo() {
    return this.classContext.getClassInfo();
  }

  public boolean shouldMutate(final MutationIdentifier newId) {
    return this.classContext.shouldMutate(newId);
  }

  // create a mutation interceptor instead
  public void disableMutations(final String reason) {
    this.mutationFindingDisabledReasons.add(reason);
  }

  public void enableMutatations(final String reason) {
    this.mutationFindingDisabledReasons.remove(reason);
  }

  public void increment() {
    this.instructionIndex = this.instructionIndex + 1;

  }

  public int currentInstructionCount() {
    return this.instructionIndex;
  }

}
