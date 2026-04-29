plugins {
    id("java")
    id("antlr")
}

group = "crml"
version = "1.0-SNAPSHOT"

dependencies {
    antlr("org.antlr:antlr4:4.9.2")
    implementation("org.antlr:antlr4:4.9.2")

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

sourceSets {
    main {
        java {
            srcDir("build/generated-src/antlr/main")
        }
    }
}

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
