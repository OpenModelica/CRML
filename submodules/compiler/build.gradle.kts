plugins {
    id("application")
    id("com.gradleup.shadow") version "9.2.0"
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":language"))
    implementation(project(":model"))
    implementation(project(":modelica")) 
    implementation(project(":util"))

    implementation("org.apache.logging.log4j:log4j-api") {
        version {
            strictly("[2.17.0]")
        }
    }
    implementation("org.apache.logging.log4j:log4j-core") {
        version {
            strictly("[2.17.0]")
        }
    }

    implementation("org.thymeleaf:thymeleaf:3.1.3.RELEASE")
    
    implementation("org.jcommander:jcommander:1.83")
    implementation("com.aventstack:extentreports:5.0.9")
    implementation("com.j2html:j2html:1.6.0")


    testImplementation(project(":util-test"))
    testImplementation(project(":test-resources"))
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.platform:junit-platform-reporting:1.10.1") 
}

/**
 * Custom task: crmlc
 */
tasks.register<JavaExec>("crmlc") {
    dependsOn("classes")
    mainClass.set("crml.compiler.CRMLC")
    classpath = sourceSets["main"].runtimeClasspath
}

/**
 * Debug task
 */
tasks.register("printSourceSetInformation") {
    doLast {
        sourceSets.forEach { srcSet ->
            println("[${srcSet.name}]")
            println("-->Source directories: ${srcSet.allJava.srcDirs}")
            println("-->Output directories: ${srcSet.output.classesDirs.files}")
            println()
        }
    }
}

application {
    mainClass.set("crml.compiler.CRMLC")
}

tasks.test {
    useJUnitPlatform {
        // Needs OpenModelica; run through verificationTest instead.
        excludeTags("omc")
    }
    testLogging {
        events("passed", "skipped", "failed")
    }
    ignoreFailures = true
}

/**
 * End-to-end verification: simulates the generated Modelica with OpenModelica
 * and compares it with the reference results. Skips every case if omc is not
 * installed. Report: build/verification_report.html.
 */
tasks.register<Test>("verificationTest") {
    group = "verification"
    description = "Simulates the generated Modelica with omc and compares it with the reference results"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("omc")
    }
    systemProperty("crml.test.report", "build/verification_report.html")
    testLogging {
        events("passed", "skipped", "failed")
    }
    ignoreFailures = true
    // omc is the bottleneck and every case reads the same libraries; running
    // in parallel would only contend for the same CPU and disk.
    maxParallelForks = 1
    outputs.upToDateWhen { false }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "crml.compiler.CRMLC",
            "Multi-Release" to "true"
        )
    }
}

// The old AST-based translation pipeline (crmlVisitorImpl/CRMLC and its ctests)
// predates the current ANTLR grammar and no longer compiles against it. It is
// being superseded by the OM-based omcv2 pipeline; kept on disk for reference
// but excluded from compilation until it is revived or removed.
sourceSets {
    main {
        java {
            exclude("crml/compiler/translation/**")
            exclude("crml/compiler/CRMLC.java")
        }
    }
    test {
        java {
            exclude("ctests/**")
        }
    }
}

// Equivalent to: ./gradlew test --tests "ctests.*TLTests"
// Writes the Modelica text of every test model as the omcv2 pipeline gives
// it, the reference that the web build is compared with:
//   ./gradlew :compiler:translateModels -Pmodels=<dir> -Pout=<dir>
// Relative directories are taken from the root of the repository.
tasks.register<JavaExec>("translateModels") {
    group = "verification"
    description = "Translates the .crml files under -Pmodels into -Pout, the reference for the web build"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("crml.compiler.util.TranslateModels")
    args = listOf(
        rootProject.file(project.findProperty("models")
            ?: "submodules/test-resources/src/main/resources/testModels").path,
        rootProject.file(project.findProperty("out") ?: "build/web-expected").path
    )
}
