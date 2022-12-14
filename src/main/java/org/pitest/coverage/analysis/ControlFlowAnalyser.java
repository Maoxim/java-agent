package org.pitest.coverage.analysis;

import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class ControlFlowAnalyser {

  private static final int LIKELY_NUMBER_OF_LINES_PER_BLOCK = 7;

  public static List<Block> analyze(final MethodNode mn) {
    final List<Block> blocks = new ArrayList<>(mn.instructions.size());

    final Set<LabelNode> jumpTargets = findJumpTargets(mn.instructions);

    // not managed to construct bytecode to show need for this
    // as try catch blocks usually have jumps at their boundaries anyway.
    // so possibly useless, but here for now. Because fear.
    addtryCatchBoundaries(mn, jumpTargets);

    Set<Integer> blockLines = smallSet();
    int lastLine = Integer.MIN_VALUE;

    final int lastInstruction = mn.instructions.size() - 1;

    int blockStart = 0;
    for (int i = 0; i != mn.instructions.size(); i++) {

      final AbstractInsnNode ins = mn.instructions.get(i);

      if (ins instanceof LineNumberNode) {
        final LineNumberNode lnn = (LineNumberNode) ins;
        blockLines.add(lnn.line);
        lastLine = lnn.line;
      } else if (jumpTargets.contains(ins) && (blockStart != i)) {
        if (blockLines.isEmpty() && blocks.size() > 0 && !blocks
            .get(blocks.size() - 1).getLines().isEmpty()) {
          blockLines.addAll(blocks.get(blocks.size() - 1).getLines());
        }
        blocks.add(new Block(blockStart, i - 1, blockLines));
        blockStart = i;
        blockLines = smallSet();
      } else if (endsBlock(ins)) {
        if (blockLines.isEmpty() && blocks.size() > 0 && !blocks
            .get(blocks.size() - 1).getLines().isEmpty()) {
          blockLines.addAll(blocks.get(blocks.size() - 1).getLines());
        }
        blocks.add(new Block(blockStart, i, blockLines));
        blockStart = i + 1;
        blockLines = smallSet();
      } else if ((lastLine != Integer.MIN_VALUE) && isInstruction(ins)) {
        blockLines.add(lastLine);
      }
    }

    // this will not create a block if the last block contains only a single
    // instruction.
    // In the case of the hanging labels that eclipse compiler seems to generate
    // this is desirable.
    // Not clear if this will create problems in other scenarios
    if (blockStart != lastInstruction) {
      blocks.add(new Block(blockStart, lastInstruction, blockLines));
    }

    return blocks;

  }

  private static HashSet<Integer> smallSet() {
    return new HashSet<>(LIKELY_NUMBER_OF_LINES_PER_BLOCK);
  }

  private static boolean isInstruction(final AbstractInsnNode ins) {
    return !((ins instanceof LabelNode) || (ins instanceof FrameNode));
  }

  private static void addtryCatchBoundaries(final MethodNode mn,
      final Set<LabelNode> jumpTargets) {
    for (final Object each : mn.tryCatchBlocks) {
      final TryCatchBlockNode tcb = (TryCatchBlockNode) each;
      jumpTargets.add(tcb.handler);
    }
  }

  private static boolean endsBlock(final AbstractInsnNode ins) {
    return (ins instanceof JumpInsnNode) || isReturn(ins)
        || isMightThrowException(ins);
  }

  private static boolean isMightThrowException(final AbstractInsnNode ins) {
    return ins.getType() == AbstractInsnNode.METHOD_INSN;
  }

  private static boolean isReturn(final AbstractInsnNode ins) {
    final int opcode = ins.getOpcode();
    switch (opcode) {
    case RETURN:
    case ARETURN:
    case DRETURN:
    case FRETURN:
    case IRETURN:
    case LRETURN:
    case ATHROW:
      return true;
    }

    return false;

  }

  private static Set<LabelNode> findJumpTargets(final InsnList instructions) {
    final Set<LabelNode> jumpTargets = new HashSet<>();
    for (AbstractInsnNode o : instructions) {
      if (o instanceof JumpInsnNode) {
        jumpTargets.add(((JumpInsnNode) o).label);
      } else if (o instanceof TableSwitchInsnNode) {
        final TableSwitchInsnNode twn = (TableSwitchInsnNode) o;
        jumpTargets.add(twn.dflt);
        jumpTargets.addAll(twn.labels);
      } else if (o instanceof LookupSwitchInsnNode) {
        final LookupSwitchInsnNode lsn = (LookupSwitchInsnNode) o;
        jumpTargets.add(lsn.dflt);
        jumpTargets.addAll(lsn.labels);
      }
    }
    return jumpTargets;
  }

}
