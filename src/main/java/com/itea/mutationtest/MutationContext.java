package com.itea.mutationtest;

import java.util.List;

public class MutationContext {
    private byte[] classFileBuffer;
//    private final List<MutationDetails> mutations = new ArrayList<>();
    private String classToMutate;
    private List<String> mutators;
    private boolean findMutations;
    private boolean getMutations;

    public MutationContext() {
    }

    public MutationContext(String classToMutate, List<String> mutators) {
        this.classToMutate = classToMutate;
        this.mutators = mutators;
        this.findMutations = true;
        getMutations = false;
    }

    public void setClassFileBuffer(byte[] classFileBuffer) {
        this.classFileBuffer = classFileBuffer;
    }

    public void setClassToMutate(String classToMutate) {
        this.classToMutate = classToMutate;
    }

    public void setMutators(List<String> mutators) {
        this.mutators = mutators;
    }

    public void setFindMutations(boolean findMutations) {
        this.findMutations = findMutations;
    }

    public void setGetMutations(boolean getMutations) {
        this.getMutations = getMutations;
    }

    public byte[] getOriginClassFileBuffer() {
        return classFileBuffer;
    }

//    public List<MutationDetails> getMutations() {
//        return mutations;
//    }

    public boolean isFindingMutations() {
        return findMutations;
    }

    public void saveClassFileBuffer(byte[] classfileBuffer) {
        this.classFileBuffer = classfileBuffer;
    }

    public boolean isGetMutations() {
        return getMutations;
    }

    public String getClassToMutate() {
        return classToMutate;
    }

    public List<String> getMutators() {
        return mutators;
    }

    public boolean isFindMutations() {
        return findMutations;
    }
}
