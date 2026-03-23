plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
}

group = "crml"
version = "1.0-SNAPSHOT"

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

subprojects {
    plugins.apply("java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }
}