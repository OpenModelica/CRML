plugins {
    id("java")
    id("antlr")
}

dependencies {
    antlr("org.antlr:antlr4:4.9.2")
    implementation("org.antlr:antlr4:4.9.2")

    implementation(project(":util"))

    // The model constraint engine (org.openmodelica.crml.testutil.constraints) works on
    // any EMF model. Versions are the ones the root build pins for the whole project.
    val emfCoreVersion: String by rootProject.extra
    val emfCommonVersion: String by rootProject.extra
    implementation("org.eclipse.emf:org.eclipse.emf.ecore:$emfCoreVersion")
    implementation("org.eclipse.emf:org.eclipse.emf.common:$emfCommonVersion")

    implementation("com.j2html:j2html:1.6.0")
    
    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    implementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.platform:junit-platform-reporting:1.10.1")
    implementation("org.junit.platform:junit-platform-launcher")

    implementation("com.aventstack:extentreports:5.0.9")
    
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
