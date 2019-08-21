package soot.dexpler.instructions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ThreeRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.Type;
import soot.LongType;
import soot.Value;
import soot.dexpler.DexBody;
import soot.dexpler.DexTypeInference;
import soot.dexpler.tags.DoubleOpTag;
import soot.dexpler.tags.FloatOpTag;
import soot.dexpler.tags.IntOpTag;
import soot.dexpler.tags.LongOpTag;
import soot.dexpler.tags.UsedRegMapTag;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;

public class BinopInstruction extends TaggedInstruction {

  public BinopInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException("Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x binOpInstr = (Instruction23x) instruction;
    int dest = binOpInstr.getRegisterA();

    DexTypeInference.checkUpdateTypeGroup(dest, binOpInstr.getRegisterB(), body);
    DexTypeInference.checkUpdateTypeGroup(binOpInstr.getRegisterC(), dest, body);
    Local source1 = DexTypeInference.applyBackward(binOpInstr.getRegisterB(), getInferredType(), body);
    Local source2 = DexTypeInference.applyBackward(binOpInstr.getRegisterC(), getInferredType(), body);

    Value expr = getExpression(source1, source2);
    Local target = DexTypeInference.applyForward(dest, getInferredType(), body);

    AssignStmt assign = Jimple.v().newAssignStmt(target, expr);
    assign.addTag(getTag());

    setUnit(assign);
    addTags(assign);
    body.add(assign);
    assign.addTag(new UsedRegMapTag(body, codeAddress,
                dest, binOpInstr.getRegisterB(), binOpInstr.getRegisterC()));
    body.setLRAssign(dest, assign);

    /*
     * if (IDalvikTyper.ENABLE_DVKTYPER) { int op = (int)instruction.getOpcode().value; BinopExpr bexpr = (BinopExpr)expr;
     * JAssignStmt jassign = (JAssignStmt)assign; DalvikTyper.v().setType(bexpr.getOp1Box(), op1BinType[op-0x90], true);
     * DalvikTyper.v().setType(bexpr.getOp2Box(), op2BinType[op-0x90], true); DalvikTyper.v().setType(jassign.leftBox,
     * resBinType[op-0x90], false); }
     */
  }

  private Type getInferredType() {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_LONG:
      case SUB_LONG:
      case MUL_LONG:
      case DIV_LONG:
      case REM_LONG:
      case AND_LONG:
      case OR_LONG:
      case XOR_LONG:
      case SHL_LONG:
      case SHR_LONG:
      case USHR_LONG:
        return LongType.v();

      case ADD_FLOAT:
      case SUB_FLOAT:
      case MUL_FLOAT:
      case DIV_FLOAT:
      case REM_FLOAT:
        return FloatType.v();

      case ADD_DOUBLE:
      case SUB_DOUBLE:
      case MUL_DOUBLE:
      case DIV_DOUBLE:
      case REM_DOUBLE:
        return DoubleType.v();

      case ADD_INT:
      case SUB_INT:
      case MUL_INT:
      case DIV_INT:
      case REM_INT:
      case AND_INT:
      case OR_INT:
      case XOR_INT:
      case SHL_INT:
      case SHR_INT:
      case USHR_INT:
        return IntType.v();

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  private Value getExpression(Local source1, Local source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_INT:
        setTag(new IntOpTag());
        return Jimple.v().newAddExpr(source1, source2);

      case SUB_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_INT:
        setTag(new IntOpTag());
        return Jimple.v().newSubExpr(source1, source2);

      case MUL_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_INT:
        setTag(new IntOpTag());
        return Jimple.v().newMulExpr(source1, source2);

      case DIV_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_INT:
        setTag(new IntOpTag());
        return Jimple.v().newDivExpr(source1, source2);

      case REM_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_INT:
        setTag(new IntOpTag());
        return Jimple.v().newRemExpr(source1, source2);

      case AND_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newAndExpr(source1, source2);
      case AND_INT:
        setTag(new IntOpTag());
        return Jimple.v().newAndExpr(source1, source2);

      case OR_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newOrExpr(source1, source2);
      case OR_INT:
        setTag(new IntOpTag());
        return Jimple.v().newOrExpr(source1, source2);

      case XOR_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newXorExpr(source1, source2);
      case XOR_INT:
        setTag(new IntOpTag());
        return Jimple.v().newXorExpr(source1, source2);

      case SHL_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newShlExpr(source1, source2);
      case SHL_INT:
        setTag(new IntOpTag());
        return Jimple.v().newShlExpr(source1, source2);

      case SHR_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newShrExpr(source1, source2);
      case SHR_INT:
        setTag(new IntOpTag());
        return Jimple.v().newShrExpr(source1, source2);

      case USHR_LONG:
        setTag(new LongOpTag());
        return Jimple.v().newUshrExpr(source1, source2);
      case USHR_INT:
        setTag(new IntOpTag());
        return Jimple.v().newUshrExpr(source1, source2);

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    ThreeRegisterInstruction i = (ThreeRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
