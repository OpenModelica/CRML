plugins {
    id("java")
    id("com.gradleup.shadow")
}

group = "crml"
version = "1.0-SNAPSHOT"

// ─────────────────────────────────────────────
//  Version catalogue
// ─────────────────────────────────────────────
val xtextVersion     = "2.33.0"
val emfCoreVersion   = "2.33.0"
val emfCommonVersion = "2.28.0"
val mwe2Version      = "2.15.0"

// ─────────────────────────────────────────────
//  Dependency resolution rules
// ─────────────────────────────────────────────
// XText's own Lexer was compiled against antlr-runtime:3.2 and references
// Token.EOF_TOKEN, a field that was removed in 3.5.x.  Pin 3.2 on every
// classpath that runs XText code.
listOf("runtimeClasspath", "testRuntimeClasspath").forEach { cfg ->
    configurations.named(cfg) {
        resolutionStrategy { force("org.antlr:antlr-runtime:3.2") }
    }
}

// ─────────────────────────────────────────────
//  MWE2 generator classpath (build-time only)
// ─────────────────────────────────────────────
val mwe2 by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

// ─────────────────────────────────────────────
//  Dependencies
// ─────────────────────────────────────────────
dependencies {
    // Shared EMF model – provides crml.model.language.* at runtime and compile time
    implementation(project(":model"))

    // ── XText runtime ─────────────────────────────────────────────────────────
    implementation("org.eclipse.xtext:org.eclipse.xtext:$xtextVersion")
    implementation("org.eclipse.xtext:org.eclipse.xtext.ide:$xtextVersion")
    implementation("org.eclipse.emf:org.eclipse.emf.ecore:$emfCoreVersion")
    implementation("org.eclipse.emf:org.eclipse.emf.common:$emfCommonVersion")

    // ── MWE2 generator (runs the XtextGenerator to produce language infra) ────
    mwe2(project(":model"))               // LanguagePackage must be on the MWE2 classpath
    mwe2("org.eclipse.emf:org.eclipse.emf.mwe2.launch:$mwe2Version")
    mwe2("org.eclipse.xtext:org.eclipse.xtext.xtext.generator:$xtextVersion")
    mwe2("org.eclipse.xtext:org.eclipse.xtext:$xtextVersion")
    mwe2("org.eclipse.xtext:org.eclipse.xtext.common.types:$xtextVersion")
    mwe2("org.eclipse.emf:org.eclipse.emf.ecore:$emfCoreVersion")
    mwe2("org.eclipse.emf:org.eclipse.emf.common:$emfCommonVersion")
    mwe2("org.eclipse.emf:org.eclipse.emf.codegen.ecore:$emfCoreVersion")
    // ANTLR 3 tool is used by XtextAntlrGeneratorFragment2 to compile the
    // generated parser grammar.  Use the patched jar (Java 21 fix for the
    // ArrayList.removeAll NPE in CompositeGrammar.getIndirectDelegates).
    mwe2(files(".antlr-generator-3.2.0-patch.jar"))

    // ── Tests ─────────────────────────────────────────────────────────────────
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.platform:junit-platform-reporting:1.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

// ─────────────────────────────────────────────
//  XText language generation via MWE2
// ─────────────────────────────────────────────
val genModelFile = project(":model")
    .layout
    .buildDirectory
    .file("generated/model.genmodel")

val xtextGenDir = layout.buildDirectory.dir("xtext-gen/main")

tasks.register<JavaExec>("generateXtextLanguage") {
    group = "build"
    description = "Generate XText language infrastructure from CrmlXtext.xtext"

    dependsOn(project(":model").tasks.named("generateGenModel"))

    classpath = mwe2 + files("src/main/mwe2")
    mainClass.set("org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher")
    args = listOf(
        "crml.language.xtext.GenerateCrmlXtext",
        "-p", "rootPath=${projectDir}",
        "-p", "genmodelPath=${genModelFile.get().asFile.absolutePath}"
    )

    inputs.file(genModelFile)
    inputs.file("src/main/java/crml/language/xtext/CrmlXtext.xtext")
    inputs.file("src/main/mwe2/crml/language/xtext/GenerateCrmlXtext.mwe2")
    
    outputs.dir(xtextGenDir)
}

// ─────────────────────────────────────────────
//  Source sets
// ─────────────────────────────────────────────
sourceSets {
    main {
        java {
            srcDir(xtextGenDir)
//            // One-time stubs (e.g. CrmlXtextStandaloneSetup) live alongside the grammar.
//            srcDir("src/main/xtext")
            srcDir("src/main/java")
        }
        resources {
            // Grammar source (.xtext) and precompiled binary (.xtextbin) must both
            // be on the classpath so the XText runtime can load the grammar.
            srcDir("src/main/xtext")
            srcDir(xtextGenDir)
            exclude("**/*.mwe2")
            exclude("**/*.java")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("generateXtextLanguage")
}
tasks.named("processResources") {
    dependsOn("generateXtextLanguage")
}

/*tasks.named("generateXtextLanguage") {
    dependsOn(project(":model").tasks.named("generateGenModel"))
}*/

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    ignoreFailures = true
}

// ─────────────────────────────────────────────
//  Language server fat-jar
// ─────────────────────────────────────────────
tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "org.eclipse.xtext.ide.server.ServerLauncher")
    }
    // Merge META-INF/services so the Xtext ISetup service loader works
    mergeServiceFiles()
    archiveClassifier.set("ls")
}
 
