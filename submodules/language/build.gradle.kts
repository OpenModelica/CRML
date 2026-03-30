plugins {
    id("java")
    id("antlr")
}

group = "crml"
version = "1.0-SNAPSHOT"

dependencies {
    antlr("org.antlr:antlr4:4.9.2")
    implementation("org.antlr:antlr4:4.9.2")



    testImplementation("com.j2html:j2html:1.6.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-reporting:1.10.1")
    testImplementation("com.aventstack:extentreports:5.0.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
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

tasks.named<Test>("test") {
    useJUnitPlatform()
}