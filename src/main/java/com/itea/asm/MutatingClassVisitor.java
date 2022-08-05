package com.itea.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

public class MutatingClassVisitor extends ClassVisitor {
    private MutationContext context;
    private List<MethodMutatorFactory> mutators;

    public MutatingClassVisitor(int api) {
        super(api);
    }

    public MutatingClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public MutatingClassVisitor(ClassVisitor classVisitor, MutationContext context, List<MethodMutatorFactory> mutators) {
        super(ASM9, classVisitor);
        this.context = context;
        this.mutators = mutators;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//        System.out.println(name + " extends " + superName + "{");
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (name.equals("hello")) {
            MethodVisitor methodVisitor = this.cv.visitMethod(access, name, descriptor, signature, exceptions);
            return new MyMethodVisitor(ASM9, methodVisitor);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
//        System.out.println("}");
        super.visitEnd();
    }
}
