package soot.dexpler;

import soot.Local;
import soot.dexpler.DexBody;
import soot.UnknownType;
import soot.DoubleType;
import soot.LongType;
import soot.jimple.Jimple;
import soot.Type;
import soot.Unit;

public class DexTypeInference {

    private static int getTypeWidth (Type type) {
        if (type instanceof DoubleType || type instanceof LongType)
            return 64;
        return 32;
    }

    public static Local applyBackward (int targetReg, Type assumeTy, DexBody body) {
        boolean useResultReg = targetReg == -1/*storeResultLocal*/;

        Local target;
        if (useResultReg)
            target = body.getStoreResultLocal();
        else
            target = body.getRegisterLocal(targetReg);

        if (target.getType() instanceof UnknownType)
            target.setType(assumeTy);
        // Skip otherwise

        return target;
    }

    public static Local applyForward (int targetReg, Type assumeTy, DexBody body) {

        boolean useResultReg = targetReg == -1/*storeResultLocal*/;

        Local target;
        if (useResultReg)
            target = body.getStoreResultLocal();
        else
            target = body.getRegisterLocal(targetReg);

        if (target.getType() instanceof UnknownType)
            // TODO hzh<huzhenghao@sbrella.com>: Maybe we could skip the type inference
            // of the 2nd register when assumeTy is Long/Double type
            target.setType(assumeTy);
        else if (!target.getType().equals(assumeTy)) {
            //if (getTypeWidth(target.getType()) != getTypeWidth(assumeTy)
            //  // NOTE hzh<huzhenghao@sbrella.com>: Return Register doesn't care about
            //  // reuses with types of different width.
            //  && !useResultReg) {
            //    System.err.println("===========================================");
            //    for (Unit u : body.getBody().getUnits())
            //        System.err.println(u);
            //    System.err.println("===========================================");
            //    throw new RuntimeException("Forward Type Inference Failed: Unmatched Reg Width: "+ target.getType() + " vs. "+ assumeTy);
            //}
            Local newlocal = Jimple.v().newLocal("tmp", assumeTy);
            newlocal.setName("tmp$"+System.identityHashCode(newlocal));

            if (useResultReg)
                body.setStoreResultLocal(newlocal);
            else
                body.setRegisterLocal(targetReg, newlocal);

            // Add new local to Jimple Body
            body.getBody().getLocals().add(newlocal);

            // LongType and DoubleType takes up 2 registers
            //if (getTypeWidth(assumeTy) == 64)
            //    body.setRegisterLocal(targetReg + 1,
            //            Jimple.v().newLocal(newlocal.getName(), assumeTy));
            return newlocal;
        }
        return target;
    }
}
