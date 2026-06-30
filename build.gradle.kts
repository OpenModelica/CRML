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
val emfCodegenVersion by extra("2.45.0")   // org.eclipse.emf.codegen.ecore (runtime)
val xcoreVersion      by extra("1.21.0")   // org.eclipse.emf.ecore.xcore
val xcoreLibVersion   by extra("1.6.0")    // org.eclipse.emf.ecore.xcore.lib
val xtextVersion      by extra("2.33.0")   // org.eclipse.xtext.*
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
        maven {
            url = uri("https://maven.pkg.github.com/folmate/emf-mermaid")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
} 

subprojects {
    plugins.apply("java")

    /*
    //Toolchan for gradle only, probably not necessary.
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }*/
    plugins.withType<JavaPlugin> {
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(8)
        }
    }
}