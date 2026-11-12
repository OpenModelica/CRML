package org.openmodelica.crml.compiler.omc;

import org.openmodelica.crml.compiler.omc.OMCUtil.OMCFilesLog;

public class OMCmsg {
        public OMCFilesLog files;
        public String msg;
  
        public OMCmsg(OMCFilesLog files, String msg){
          this.files=files;
          this.msg=msg;
        }
}
