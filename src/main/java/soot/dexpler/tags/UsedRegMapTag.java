
package soot.dexpler.tags;

import soot.dexpler.DexBody;
import soot.tagkit.Tag;
import java.util.HashMap;
import java.util.Set;

public class UsedRegMapTag implements Tag {
  private HashMap<String, String> regNames;

  public UsedRegMapTag(HashMap<String, String> mapping) {
    this.regNames = mapping;
  }

  public UsedRegMapTag() { this.regNames = new HashMap<String, String>(); }
  public UsedRegMapTag(DexBody body, int codeaddr, int reg) {
    this();
    this.setRegMapping(body, codeaddr, reg);
  }
  public UsedRegMapTag(DexBody body, int codeaddr, int reg1, int reg2) {
    this();
    this.setRegMapping(body, codeaddr, reg1);
    this.setRegMapping(body, codeaddr, reg2);
  }
  public UsedRegMapTag(DexBody body, int codeaddr, int reg1, int reg2, int reg3) {
    this();
    this.setRegMapping(body, codeaddr, reg1);
    this.setRegMapping(body, codeaddr, reg2);
    this.setRegMapping(body, codeaddr, reg3);
  }

  /** Returns the tag name. */
  public String getName() {
    return "UsedRegMapTag";
  }

  /** Returns the tag raw data. */
  public byte[] getValue() {
    throw new RuntimeException("UsedRegMapTag has no value for bytecode");
  }

  public void setRegMapping(DexBody body, int codeaddr, int reg) {
    String regname = body.getRegisterLocal(reg).getName();
    String chkname = body.getValidVarName(codeaddr, reg);
    //System.err.println(""+codeaddr+": "+reg+" -> "+regname+" > "+chkname);
    regNames.put(regname, chkname);
  }

  public String getRegMapping(String key) {
    return regNames.get(key);
  }

  public String toString() {
    String out = "";
    for (String k : regNames.keySet()) {
      out += k + "=>" + regNames.get(k) + ";\n";
    }
    return out;
  }
}
