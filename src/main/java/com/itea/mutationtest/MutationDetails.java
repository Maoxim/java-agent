package com.itea.mutationtest;

public class MutationDetails {
    private String classToMutate;
    private String method;
    private int lineNumber;
    private int instructionIndex;

    public MutationDetails(String classToMutate, String method, int lineNumber, int instructionIndex) {
        this.classToMutate = classToMutate;
        this.method = method;
        this.lineNumber = lineNumber;
        this.instructionIndex = instructionIndex;
    }
}
