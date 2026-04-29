package crml.language.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import crml.util.SafeResource;

public class SpecsRoot {
    public static Path RESOURCES = SafeResource.get("specs/specRoot.txt").getParent();
    public static Path OUT = Paths.get("build","testSuiteGenerated");
}