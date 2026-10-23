/*
 * The CRML to Modelica translator for web browsers: the parser, crml.language
 * and crml.compiler.crmlcv2 compiled with TeaVM to JavaScript
 * (build/generated/teavm/js/crml.js) and WebAssembly
 * (build/generated/teavm/wasm-gc/crml.wasm + crml.wasm-runtime.js).
 * The sources are those of the language and compiler projects; the object
 * model comes from the model and modelica jars. Only the entry point,
 * crml.web.CrmlWeb, and what EMF needs to run in a browser are here: stand-ins
 * for log4j, Eclipse/OSGi and the JDK classes TeaVM lacks, and the TeaVM
 * plug-in and substitution policy in crml.web.teavm.
 */
plugins {
    id("java")
    id("antlr")
    id("org.teavm") version "0.16.0"
}

group = "crml"
version = "1.0-SNAPSHOT"

dependencies {
    // ANTLR 4.9.2, used by the language project, reads its parser tables with
    // java.util.UUID, which TeaVM does not provide; 4.10 and later do not.
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation(teavm.libs.jsoApis)

    // The object model (EMF/Xcore) that crml.language builds and
    // crml.compiler.crmlcv2 translates, used as jars: only the parser has to
    // be regenerated here.
    implementation(project(":model"))
    implementation(project(":modelica"))
    implementation("org.apache.commons:commons-lang3:3.20.0")

    // For the plug-in and substitution policy in crml.web.teavm, which TeaVM
    // loads while it compiles.
    compileOnly("org.teavm:teavm-core:0.16.0")
    compileOnly("org.teavm:teavm-extension-spi:0.16.0")
}

// modelica puts the EMF code generator, with the Eclipse platform and JDT, on
// its runtime classpath; none of it runs in a browser.
configurations.all {
    exclude(group = "org.eclipse.emf", module = "org.eclipse.emf.codegen.ecore")
    exclude(group = "org.eclipse.emf", module = "org.eclipse.emf.codegen")
}

sourceSets {
    main {
        antlr {
            setSrcDirs(listOf("../language/src/main/antlr"))
        }
        java {
            srcDir("../language/src/main/java")
            srcDir("../compiler/src/main/java")
            include("crml/web/**", "org/apache/logging/log4j/**", "org/teavm/classlib/**",
                    "org/eclipse/core/runtime/**", "org/osgi/framework/**", "crml/language/**",
                    "crml/compiler/crmlcv2/**")
        }
    }
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments.addAll(listOf("-visitor", "-package", "crml.language.grammar"))
}

// The sources are compiled for Java 8, as in the other projects, against the
// TeaVM and ANTLR libraries, which are built for newer Java versions.
java {
    disableAutoTargetJvm()
}

// TeaVM 0.16 optimizes the EMF code differently on each build, so that
// crml.js and crml.wasm change without their sources changing. Unoptimized,
// they are reproducible and as fast for the test models; crml.js is ~13%
// larger, crml.wasm ~20% smaller.
teavm {
    js {
        mainClass = "crml.web.CrmlWeb"
        targetFileName = "crml.js"
        moduleType = org.teavm.gradle.api.JSModuleType.ES2015
        obfuscated = true
        optimization = org.teavm.gradle.api.OptimizationLevel.NONE
    }
    wasmGC {
        mainClass = "crml.web.CrmlWeb"
        targetFileName = "crml.wasm"
        optimization = org.teavm.gradle.api.OptimizationLevel.NONE
    }
}

tasks.register("web") {
    group = "build"
    description = "Builds the JavaScript and WebAssembly translators."
    dependsOn("generateJavaScript", "buildWasmGC")
}

// The TeaVM 0.16 wasm-gc runtime makes each java.lang.ref.WeakReference a JS
// WeakRef, and new WeakRef(null) throws, but new WeakReference(null) is legal
// Java and EMF makes one (WeakInterningHashSet.NULL_ENTRY). A null referent
// becomes an object whose deref() is undefined, which the runtime reads as null.
tasks.named("copyWasmGCRuntime") {
    doLast {
        outputs.files.asFileTree.matching { include("*.js") }.forEach { file ->
            val text = file.readText()
            val patched = text.replace(Regex("""return new WeakRef\((\w+)\)""")) {
                val v = it.groupValues[1]
                "return $v === null ? { deref() { } } : new WeakRef($v)"
            }
            check(patched != text) { "No WeakRef to patch in ${file.name}; check the TeaVM runtime" }
            file.writeText(patched)
        }
    }
}
