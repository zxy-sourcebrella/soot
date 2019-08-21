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
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;

import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Type;
import soot.Value;
import soot.dexpler.DexBody;
import soot.dexpler.tags.DoubleOpTag;
import soot.dexpler.tags.FloatOpTag;
import soot.dexpler.tags.IntOpTag;
import soot.dexpler.tags.LongOpTag;
import soot.dexpler.tags.UsedRegMapTag;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.dexpler.DexTypeInference;

public class Binop2addrInstruction extends TaggedInstruction {

  public Binop2addrInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException("Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x binOp2AddrInstr = (Instruction12x) instruction;
    int dest = binOp2AddrInstr.getRegisterA();

    DexTypeInference.checkUpdateTypeGroup(dest, binOp2AddrInstr.getRegisterB(), body);
    Local source1 = DexTypeInference.applyBackward(binOp2AddrInstr.getRegisterA(), getInferredType(), body);
    Local source2 = DexTypeInference.applyBackward(binOp2AddrInstr.getRegisterB(), getInferredType(), body);

    Value expr = getExpression(source1, source2);

    AssignStmt assign = Jimple.v().newAssignStmt(body.getRegisterLocal(dest), expr);
    assign.addTag(getTag());

    setUnit(assign);
    addTags(assign);
    body.add(assign);
    assign.addTag(new UsedRegMapTag(body, codeAddress,
                dest, binOp2AddrInstr.getRegisterB()));
    body.setLRAssign(dest, assign);

    /*
     * if (IDalvikTyper.ENABLE_DVKTYPER) { BinopExpr bexpr = (BinopExpr)expr; short op = instruction.getOpcode().value;
     * DalvikTyper.v().setType(bexpr.getOp1Box(), op1BinType[op-0xb0], true); DalvikTyper.v().setType(bexpr.getOp2Box(),
     * op2BinType[op-0xb0], true); DalvikTyper.v().setType(assign.getLeftOpBox(), resBinType[op-0xb0], false); }
     */
  }

  private Type getInferredType() {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_LONG_2ADDR:
      case SUB_LONG_2ADDR:
      case MUL_LONG_2ADDR:
      case DIV_LONG_2ADDR:
      case REM_LONG_2ADDR:
      case AND_LONG_2ADDR:
      case OR_LONG_2ADDR:
      case XOR_LONG_2ADDR:
      case SHL_LONG_2ADDR:
      case SHR_LONG_2ADDR:
      case USHR_LONG_2ADDR:
        return LongType.v();

      case ADD_FLOAT_2ADDR:
      case SUB_FLOAT_2ADDR:
      case MUL_FLOAT_2ADDR:
      case DIV_FLOAT_2ADDR:
      case REM_FLOAT_2ADDR:
        return FloatType.v();

      case ADD_DOUBLE_2ADDR:
      case SUB_DOUBLE_2ADDR:
      case MUL_DOUBLE_2ADDR:
      case DIV_DOUBLE_2ADDR:
      case REM_DOUBLE_2ADDR:
        return DoubleType.v();

      case ADD_INT_2ADDR:
      case SUB_INT_2ADDR:
      case MUL_INT_2ADDR:
      case DIV_INT_2ADDR:
      case REM_INT_2ADDR:
      case AND_INT_2ADDR:
      case OR_INT_2ADDR:
      case XOR_INT_2ADDR:
      case SHL_INT_2ADDR:
      case SHR_INT_2ADDR:
      case USHR_INT_2ADDR:
        return IntType.v();

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  private Value getExpression(Local source1, Local source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_FLOAT_2ADDR:
        setTag(new FloatOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_DOUBLE_2ADDR:
        setTag(new DoubleOpTag());
        return Jimple.v().newAddExpr(source1, source2);
      case ADD_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newAddExpr(source1, source2);

      case SUB_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_FLOAT_2ADDR:
        setTag(new FloatOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_DOUBLE_2ADDR:
        setTag(new DoubleOpTag());
        return Jimple.v().newSubExpr(source1, source2);
      case SUB_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newSubExpr(source1, source2);

      case MUL_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_FLOAT_2ADDR:
        setTag(new FloatOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_DOUBLE_2ADDR:
        setTag(new DoubleOpTag());
        return Jimple.v().newMulExpr(source1, source2);
      case MUL_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newMulExpr(source1, source2);

      case DIV_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_FLOAT_2ADDR:
        setTag(new FloatOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_DOUBLE_2ADDR:
        setTag(new DoubleOpTag());
        return Jimple.v().newDivExpr(source1, source2);
      case DIV_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newDivExpr(source1, source2);

      case REM_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_FLOAT_2ADDR:
        setTag(new FloatOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_DOUBLE_2ADDR:
        setTag(new DoubleOpTag());
        return Jimple.v().newRemExpr(source1, source2);
      case REM_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newRemExpr(source1, source2);

      case AND_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newAndExpr(source1, source2);
      case AND_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newAndExpr(source1, source2);

      case OR_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newOrExpr(source1, source2);
      case OR_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newOrExpr(source1, source2);

      case XOR_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newXorExpr(source1, source2);
      case XOR_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newXorExpr(source1, source2);

      case SHL_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newShlExpr(source1, source2);
      case SHL_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newShlExpr(source1, source2);

      case SHR_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newShrExpr(source1, source2);
      case SHR_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newShrExpr(source1, source2);

      case USHR_LONG_2ADDR:
        setTag(new LongOpTag());
        return Jimple.v().newUshrExpr(source1, source2);
      case USHR_INT_2ADDR:
        setTag(new IntOpTag());
        return Jimple.v().newUshrExpr(source1, source2);

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

}
