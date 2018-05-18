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

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.reference.TypeReference;

import soot.IntType;
import soot.Local;
import soot.Type;
import soot.dexpler.DexBody;
import soot.dexpler.DexType;
import soot.dexpler.DexTypeInference;
import soot.dexpler.IDalvikTyper;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.Jimple;
import soot.dexpler.tags.UsedRegMapTag;

public class InstanceOfInstruction extends DexlibAbstractInstruction {

  public InstanceOfInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    Instruction22c i = (Instruction22c) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();

    Type t = DexType.toSoot((TypeReference) (i.getReference()));

    InstanceOfExpr e = Jimple.v().newInstanceOfExpr(body.getRegisterLocal(source), t);
    Local target = DexTypeInference.applyForward(dest, IntType.v(), body);
    AssignStmt assign = Jimple.v().newAssignStmt(target, e);
    setUnit(assign);
    addTags(assign);
    body.add(assign);
    assign.addTag(new UsedRegMapTag(body, codeAddress, dest, source));

    if (IDalvikTyper.ENABLE_DVKTYPER) {
      // DalvikTyper.v().?
    }
  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

  @Override
  public Set<Type> introducedTypes() {
    ReferenceInstruction i = (ReferenceInstruction) instruction;

    Set<Type> types = new HashSet<Type>();
    types.add(DexType.toSoot((TypeReference) i.getReference()));
    return types;
  }
}
