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
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    ignoreFailures = true
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "crml.compiler.CRMLC",
            "Multi-Release" to "true"
        )
    }
}

// Equivalent to: ./gradlew test --tests "ctests.*TLTests"