package org.openmodelica.crml.compiler.util;

import java.util.stream.Stream;

import org.openmodelica.crml.compiler.omc.OMCUtil.OMCFilesLog;
import org.openmodelica.crml.testutil.CustomHtmlReporter;

import static j2html.TagCreator.p;
import static j2html.TagCreator.a;
import static j2html.TagCreator.join;

public class FilesWrapper implements CustomHtmlReporter{
    private final OMCFilesLog data;
    public FilesWrapper(OMCFilesLog data){
        this.data = data;
    }

    @Override
        public Object report() {
          return join(Stream.of(data.files()).<Object>map(path -> 
              p(a(path.toString()).withHref(path.toUri().toString()))
          ).toArray());
        }
    public static FilesWrapper of(OMCFilesLog data){
        return new FilesWrapper(data);
    }
}