package crml.compiler.crmlcv2.specification;

import java.nio.file.Path;
import java.util.List;

/**
 * The models that exercise lifting: an operator applied to {@code x.element} is
 * evaluated for every element of {@code x}.
 */
public class Lifting extends OMCv2SpecificationTest {
    static List<Path> fileNameSource() {
        return modelsIn("lifting");
    }
}
