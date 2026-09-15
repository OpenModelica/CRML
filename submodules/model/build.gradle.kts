plugins {
    id("java-library")
    id("application")
    id("org.xtext.builder") version "4.0.0"
}

application {
    mainClass.set("crml.model.Main")
}

group = "org.example"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()

    // emf-mermaid is published to GitHub Packages, which needs credentials.
    // Only the "mermaid" source set and its generateMermaid task resolve
    // anything from here, so a build without credentials is unaffected.
    // The content filter keeps Gradle from consulting this repository for any
    // other module, so a missing credential can only ever surface in
    // generateMermaid.
    maven {
        name = "emf-mermaid"
        url = uri("https://maven.pkg.github.com/folmate/emf-mermaid")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
        content {
            includeGroup("io.github.folmate.ecore2mermaid")
        }
    }
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
//  Version catalogue (defined in root build.gradle.kts)
// ─────────────────────────────────────────────
val emfCoreVersion:    String by rootProject.extra
val emfCommonVersion:  String by rootProject.extra
val emfCodegenVersion: String by rootProject.extra
val xcoreVersion:      String by rootProject.extra
val xcoreLibVersion:   String by rootProject.extra
val xtextVersion:      String by rootProject.extra

// ─────────────────────────────────────────────
//  Dependencies
// ─────────────────────────────────────────────
dependencies {
    implementation("org.eclipse.emf:org.eclipse.emf.codegen.ecore:$emfCodegenVersion")

    // ── Xcore language support (build-time only) ──────────────────────────
    // These go on the classpath used by the Xtext builder to process .xcore
    // files and drive the EMF code generator.
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.ecore.xcore:$xcoreVersion")
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:$xcoreLibVersion")
    xtextLanguages("org.eclipse.emf:org.eclipse.emf.codegen.ecore:$emfCoreVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext:$xtextVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext.common.types:$xtextVersion")
    xtextLanguages("org.eclipse.xtext:org.eclipse.xtext.ecore:$xtextVersion")

    // ── Runtime dependencies exposed as api ───────────────────────────────
    // Declared api so dependents (e.g. :language) get EObject, EList, etc.
    // on their compile classpath without re-declaring these artifacts.
    api("org.eclipse.emf:org.eclipse.emf.ecore:$emfCoreVersion")
    api("org.eclipse.emf:org.eclipse.emf.common:$emfCommonVersion")
    api("org.eclipse.emf:org.eclipse.emf.ecore.xcore.lib:$xcoreLibVersion")
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

    // Mermaid diagram generation, kept out of the main source set so that
    // neither the jar nor any dependent project carries emf-mermaid, and so
    // that "gradlew build" never resolves it. Nothing in assemble or check
    // compiles this source set; only generateMermaid does.
    create("mermaid") {
        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

// Give the mermaid source set the same EMF dependencies as main, so
// MermaidMain can see the generated LanguagePackage.
configurations["mermaidImplementation"].extendsFrom(configurations["implementation"])
configurations["mermaidRuntimeOnly"].extendsFrom(configurations["runtimeOnly"])

dependencies {
    "mermaidImplementation"("io.github.folmate.ecore2mermaid:core:0.0.1")
}

// emf-mermaid lives behind GitHub Packages, so say so plainly instead of
// letting the build fail with "Username must not be null!" or a bare 403.
val hasMermaidCredentials =
    (project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")) != null &&
    (project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")) != null

// A separate task rather than a doFirst on the compile: Gradle resolves a
// task's classpath before running its actions, so a doFirst would never get
// the chance to speak.
val checkMermaidCredentials by tasks.registering {
    group = "verification"
    description = "Fails with an explanation when the emf-mermaid credentials are missing"
    doLast {
        if (!hasMermaidCredentials) {
            throw GradleException(
                "generateMermaid needs emf-mermaid from GitHub Packages, which requires credentials. " +
                "Set gpr.user and gpr.key in ~/.gradle/gradle.properties, or the GITHUB_ACTOR and " +
                "GITHUB_TOKEN environment variables, with a token that has read:packages. " +
                "No other task needs them."
            )
        }
    }
}

tasks.named("compileMermaidJava") {
    dependsOn(checkMermaidCredentials)
}

tasks.register<JavaExec>("generateMermaid") {
    group = "code generation"
    description = "Generates a Mermaid class diagram from the CRML EPackage (needs GitHub Packages credentials)"

    val outputFile = layout.buildDirectory.file("generated/crml-diagram.mmd")
    outputs.file(outputFile)

    // Carries its own task dependencies, so compileMermaidJava runs first.
    classpath = sourceSets["mermaid"].runtimeClasspath
    mainClass.set("crml.model.MermaidMain")
    args = listOf(outputFile.get().asFile.absolutePath)
}

tasks.register<JavaExec>("generateGenModel") {
    group = "code generation"
    description = "Generates EMF GenModel"
    
    val outputFile = layout.buildDirectory.file("generated/model.genmodel")

    outputs.file(outputFile)

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("crml.model.GenmodelMain")     
    
    args = listOf(
        "crml.model",
        outputFile.get().asFile.absolutePath
    )
}

