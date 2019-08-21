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

import org.jf.dexlib2.iface.instruction.Instruction;

import soot.dexpler.DexBody;
import soot.jimple.GotoStmt;
import soot.jimple.Jimple;
import soot.Unit;

public class GotoInstruction extends JumpInstruction implements DeferableInstruction {
  public GotoInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  public void jimplify(DexBody body) {

    body.takeRegSnapshot(getTargetInstruction(body).getCodeAddress());

    // check if target instruction has been jimplified
    if (getTargetInstruction(body).getUnit() != null) {
      body.add(gotoStatement(body));
      return;
    }
    // set marker unit to swap real gotostmt with otherwise
    body.addDeferredJimplification(this);
    markerUnit = Jimple.v().newNopStmt();
    addTags(markerUnit);
    unit = markerUnit;
    body.add(markerUnit);
  }

  public void deferredJimplify(DexBody body) {
    body.getBody().getUnits().insertAfter(gotoStatement(body), markerUnit);
  }

  private GotoStmt gotoStatement(DexBody body) {
    Unit u = body.getRelocatedStmt(targetInstruction.getCodeAddress());
    if (u == null)  u = targetInstruction.getUnit();
    GotoStmt go = Jimple.v().newGotoStmt(u);
    setUnit(go);
    addTags(go);
    return go;
  }

}
