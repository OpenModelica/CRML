plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
}

group = "crml"
version = "1.0-SNAPSHOT"

// ─────────────────────────────────────────────
//  Shared version catalogue
// ─────────────────────────────────────────────
val emfCoreVersion    by extra("2.33.0")   // org.eclipse.emf.ecore + codegen in xtextLanguages
val emfCommonVersion  by extra("2.28.0")   // org.eclipse.emf.common
val emfCodegenVersion by extra("2.45.0")   // org.eclipse.emf.codegen.ecore (generateGenModel only)
val xcoreVersion      by extra("1.21.0")   // org.eclipse.emf.ecore.xcore
val xcoreLibVersion   by extra("1.6.0")    // org.eclipse.emf.ecore.xcore.lib
val xtextVersion      by extra("2.33.0")   // org.eclipse.xtext.* (build-time Xcore generator only)
val xbaseLibVersion   by extra("2.25.0")   // org.eclipse.xtext.xbase.lib at runtime; last release built for Java 8
val antlr4Version     by extra("4.9.2")    // org.antlr:antlr4

allprojects {
    repositories {
        mavenCentral()
       /* maven {
            url = uri("https://repo.eclipse.org/content/repositories/viatra2-releases/")
        }
        maven {
            url = uri("https://repo.eclipse.org/content/groups/releases/")
        }*/
    }
}

// The GitHub Packages repository that hosts emf-mermaid is declared in
// :model, the one project that can use it, and only the optional
// generateMermaid task resolves anything from it. Building the project needs
// no GitHub credentials.

subprojects {
    plugins.apply("java")

    /*
    //Toolchan for gradle only, probably not necessary.
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }*/
}

// Compile with any JDK that can run Gradle (17+), but emit Java 8 bytecode
// against the Java 8 API, since the compiler must run on a Java 8 JRE.
// allprojects, so the root project's java plugin is held to 8 as well.
allprojects {
    plugins.withType<JavaPlugin> {
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(8)
        }
    }
}