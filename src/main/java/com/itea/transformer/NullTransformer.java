package com.itea.transformer;

import com.itea.mutationtest.MutationContext;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class NullTransformer implements ClassFileTransformer {
    private MutationContext context;
    private String targetClassName;

    public NullTransformer(MutationContext context, String targetClassName) {
        this.context = context;
        this.targetClassName = targetClassName;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.replace("/", ".").equals(targetClassName)) {
            context.setClassFileBuffer(classfileBuffer);
        }
        return classfileBuffer;
    }
}
