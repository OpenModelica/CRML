package crml.compiler.crmlcv2.specification;

import java.nio.file.Path;
import java.util.List;

/**
 * The ETL library's test models. These are the first corpus that exercises
 * user-defined operators and their call sites, so they only produce meaningful
 * Modelica once operators are translated.
 */
public class LibraryETL extends OMCv2SpecificationTest {
    static List<Path> fileNameSource() {
        return modelsIn("libraries", "ETL_test");
    }
}
