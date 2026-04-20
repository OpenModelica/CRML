plugins {
    id("java")
    id("application")
    id("org.xtext.builder") version "4.0.0"
}

application {
    mainClass.set("crml.model.Main")
}

group = "org.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

// ─────────────────────────────────────────────
//  Configurations
// ─────────────────────────────────────────────
val plantUml by configurations.creating

// ─────────────────────────────────────────────
//  Dependency resolution rules
// ─────────────────────────────────────────────
configurations.all {
    resolutionStrategy {
        // antlr-runtime is published as "3.2" on Maven Central but the Xcore
        // POM declares the range [3.2.0, 3.2.1).  Force the available version.
        force("org.antlr:antlr-runtime:3.2")
    }
}

// ─────────────────────────────────────────────
//  Version catalogue
// ─────────────────────────────────────────────
val xtextVersion     = "2.33.0"
val xcoreVersion     = "1.21.0"
val xcoreLibVersion  = "1.6.0"
val emfCoreVersion   = "2.33.0"
val emfCommonVersion = "2.28.0"

// ─────────────────────────────────────────────
//  Dependencies
// ─────────────────────────────────────────────
dependencies {

    // ── Xcore language support (build-time only) ──────────────────────────
    // These go on the classpath used by the Xtext builder to process .xcore
    // files and drive the EMF code generator.
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.ecore.xcore:$xcoreVersion")
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:$xcoreLibVersion")
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.codegen.ecore:$emfCoreVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext:$xtextVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext.common.types:$xtextVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext.ecore:$xtextVersion")

    // ── Runtime dependencies needed by the generated Java code ────────────
    implementation("org.eclipse.emf:org.eclipse.emf.ecore:$emfCoreVersion")
    implementation("org.eclipse.emf:org.eclipse.emf.common:$emfCommonVersion")
    implementation("org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:$xcoreLibVersion")
}

// ─────────────────────────────────────────────
//  Xtext / Xcore builder configuration
// ─────────────────────────────────────────────
xtext {
    // Must match the Xtext artifacts declared above.
    version.set(xtextVersion)

    // ── Source directories containing .xcore model files ──────────────────
    sourceSets {
        getByName("main") {
            srcDir("src/main/model")
        }
    }

    languages {
        // Ecore language support – needed as a base for Xcore
        create("ecore") {
            setup.set("org.eclipse.xtext.ecore.EcoreSupport")
        }
        // Xcore language – generates Java implementation classes
        create("xcore") {
            setup.set("org.eclipse.emf.ecore.xcore.XcoreStandaloneSetup")
            generator {
                // producesJava = true registers the outlet as a Java source
                // root so javac picks up the generated classes automatically.
                outlet {
                    producesJava.set(true)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Source sets
// ─────────────────────────────────────────────
sourceSets {
    main {
        java {
            srcDir("src/main/java")
        }
        // Prevent .xcore files from being copied into the output JAR
        resources {
            exclude("**/*.xcore")
        }
    }
}
