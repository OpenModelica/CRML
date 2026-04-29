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