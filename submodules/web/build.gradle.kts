/*
 * The CRML to Modelica translator for web browsers: the parser, crml.language
 * and crml.compiler.translation compiled with TeaVM to JavaScript
 * (build/generated/teavm/js/crml.js) and WebAssembly
 * (build/generated/teavm/wasm-gc/crml.wasm + crml.wasm-runtime.js).
 * The sources are those of the language and compiler projects; only the
 * entry point, crml.web.CrmlWeb, and a stand-in for the log4j API are here.
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
}

sourceSets {
    main {
        antlr {
            setSrcDirs(listOf("../language/src/main/antlr"))
        }
        java {
            srcDir("../language/src/main/java")
            srcDir("../compiler/src/main/java")
            include("crml/web/**", "org/apache/logging/log4j/**", "crml/language/**",
                    "crml/compiler/translation/**")
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

teavm {
    js {
        mainClass = "crml.web.CrmlWeb"
        targetFileName = "crml.js"
        moduleType = org.teavm.gradle.api.JSModuleType.ES2015
        obfuscated = true
    }
    wasmGC {
        mainClass = "crml.web.CrmlWeb"
        targetFileName = "crml.wasm"
    }
}

tasks.register("web") {
    group = "build"
    description = "Builds the JavaScript and WebAssembly translators."
    dependsOn("generateJavaScript", "buildWasmGC")
}
