package crml.compiler.omc;

import java.util.ArrayList;
import java.util.List;

import crml.compiler.omc.OMCUtil.OMCFilesLog;

public class OMCmsg {
        public OMCFilesLog files;
        public String msg;

        public OMCmsg(OMCFilesLog files, String msg){
          this.files=files;
          this.msg=msg;
        }

        /**
         * Variables compared against the reference result,
         * or null if no comparison was made
         */
        public List<String> comparedVars() {
          return markedBlock("CRML-COMPARED-VARS");
        }

        /**
         * Variables that differ from the reference result,
         * or null if no comparison was made
         */
        public List<String> diffFailedVars() {
          return markedBlock("CRML-DIFF-FAILED-VARS");
        }

        /**
         * Extracts the lines between [marker-BEGIN] and [marker-END]
         * printed by the compare_simulation_results template
         */
        private List<String> markedBlock(String marker) {
          if (msg == null)
            return null;
          String begin = "[" + marker + "-BEGIN]";
          String end = "[" + marker + "-END]";
          int b = msg.indexOf(begin);
          if (b < 0)
            return null;
          int e = msg.indexOf(end, b);
          if (e < 0)
            e = msg.length();

          List<String> vars = new ArrayList<>();
          for (String line : msg.substring(b + begin.length(), e).split("\\R")) {
            String v = line.trim();
            if (!v.isEmpty())
              vars.add(v);
          }
          return vars;
        }
}
