package org.openmodelica.crml.language.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openmodelica.crml.util.SafeResource;

public class SpecsRoot {
    public static Path RESOURCES = SafeResource.get("specs/specRoot.txt").getParent();
    public static Path OUT = Paths.get("build","testSuiteGenerated");
}