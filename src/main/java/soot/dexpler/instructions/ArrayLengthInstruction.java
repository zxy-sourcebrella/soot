/* Soot - a Java Optimization Framework
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 * 
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.dexpler.instructions;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;

import soot.IntType;
import soot.Local;
import soot.dexpler.DexBody;
import soot.dexpler.DexTypeInference;
import soot.dexpler.IDalvikTyper;
import soot.dexpler.typing.DalvikTyper;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.LengthExpr;
import soot.dexpler.tags.UsedRegMapTag;

public class ArrayLengthInstruction extends DexlibAbstractInstruction {

  public ArrayLengthInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException("Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x lengthOfArrayInstruction = (Instruction12x) instruction;
    int dest = lengthOfArrayInstruction.getRegisterA();

    Local arrayReference = DexTypeInference.applyBackward(lengthOfArrayInstruction.getRegisterB(), IntType.v().makeArrayType(), body);

    LengthExpr lengthExpr = Jimple.v().newLengthExpr(arrayReference);
    Local target = DexTypeInference.applyForward(dest, IntType.v(), body);

    AssignStmt assign = Jimple.v().newAssignStmt(target, lengthExpr);

    setUnit(assign);
    addTags(assign);
    body.add(assign);
    assign.addTag(new UsedRegMapTag(body, codeAddress,
                dest, lengthOfArrayInstruction.getRegisterB()));

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      DalvikTyper.v().setType(assign.getLeftOpBox(), IntType.v(), false);
    }
  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }
}
