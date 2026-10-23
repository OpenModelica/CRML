package crml.compiler.crmlcv2.specification;

import java.nio.file.Path;
import java.util.List;

import crml.test.TestResourcesRoot;

public class TypePeriod extends OMCv2SpecificationTest {
    static List<Path> fileNameSource() {
        return TestResourcesRoot.listFiles(SPEC_DOC_EXAMPLES, f -> {
            String name = f.getFileName().toString();
            return name.startsWith("Period") && !name.startsWith("Periods");
        });
    }
}
