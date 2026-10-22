package crml.compiler.crmlcv2.specification;

import java.nio.file.Path;
import java.util.List;

/** The FORM-L library's test models. */
public class LibraryFORML extends OMCv2SpecificationTest {
    static List<Path> fileNameSource() {
        return modelsIn("libraries", "FORML_test");
    }
}
