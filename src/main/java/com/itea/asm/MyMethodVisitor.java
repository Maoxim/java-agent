package com.itea.asm;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.POP;

public class MyMethodVisitor extends MethodVisitor {
    private final int maxLocals = 0;

    public MyMethodVisitor(int api) {
        super(api);
    }

    public MyMethodVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN) {
            mv.visitInsn(POP);
            mv.visitLdcInsn("pitest");
            mv.visitInsn(ARETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }

    public int getMaxLocals() {
        return maxLocals;
    }
}
