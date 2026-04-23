package crml.compiler.omc;

import crml.compiler.omc.OMCUtil.OMCFilesLog;

public class OMCmsg {
        public OMCFilesLog files;
        public String msg;
  
        public OMCmsg(OMCFilesLog files, String msg){
          this.files=files;
          this.msg=msg;
        }
}
