package com.itea.transformer;

import com.itea.agent.Logger;
import com.itea.mutationtest.MutationContext;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ReturnValsTransformer implements ClassFileTransformer {
    private final byte[] mutation;

    public ReturnValsTransformer(MutationContext context, String targetClassName, byte[] mutation) {
        this.mutation = mutation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        return mutation;
    }
}
