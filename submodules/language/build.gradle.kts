plugins {
    id("java")
    id("antlr")
}

group = "crml"
version = "1.0-SNAPSHOT"

val antlr4Version: String by rootProject.extra

// ─────────────────────────────────────────────
//  Dependencies
// ─────────────────────────────────────────────
dependencies {
    // Model submodule – language depends on the EMF model definition.
    // EMF (EObject, EList, etc.) reaches this module transitively via model's api deps.
    implementation(project(":model"))

    // ── ANTLR4 ───────────────────────────────────────────────────────────────
    antlr("org.antlr:antlr4:$antlr4Version")
    implementation("org.antlr:antlr4:$antlr4Version")
    implementation("org.apache.commons:commons-lang3:3.20.0")

    testImplementation(project(":util"))
    testImplementation(project(":util-test"))

    testImplementation("com.j2html:j2html:1.6.0")
    
    testImplementation(project(":test-resources"))
    
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.platform:junit-platform-reporting:1.10.1")
    testImplementation("org.junit.platform:junit-platform-launcher")

    testImplementation("com.aventstack:extentreports:5.0.9")
}

// ─────────────────────────────────────────────
//  ANTLR grammar generation
// ─────────────────────────────────────────────
tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments.addAll(listOf(
        "-visitor",
        "-long-messages",
        "-Xlog",
        "-listener",
        "-package", "crml.language.grammar",
        "-lib", "src/main/antlr/crml/language/grammar"
    ))
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
    ignoreFailures = true
}
