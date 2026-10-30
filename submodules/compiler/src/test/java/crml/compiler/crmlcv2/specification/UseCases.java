package crml.compiler.crmlcv2.specification;

import java.nio.file.Path;
import java.util.List;

/** The use-case models: the largest inputs in the corpus. */
public class UseCases extends OMCv2SpecificationTest {
    static List<Path> fileNameSource() {
        return modelsIn("use-cases");
    }
}
