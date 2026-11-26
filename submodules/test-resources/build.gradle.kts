plugins {
    id("java")
}

dependencies {
    implementation(project(":util"))
}

sourceSets {
    main {
        resources {
            // Appends an external directory to the build reources
            srcDir("${rootProject.projectDir}/resources/crml_tutorial")
        }
    }
}

/**
 * The EDF CRML library, which the verification harnesses (verificationModels/)
 * are built on. Its only copy is in libraries/modelica; it is packaged as the
 * package directory modelica_libraries/CRML/ (load CRML/package.mo).
 */
tasks.processResources {
    from(rootProject.file("libraries/modelica/CRML")) {
        into("modelica_libraries/CRML")
    }
}

tasks.register("debugResources") {
    doLast {
        sourceSets["main"].resources.srcDirs.forEach {
            println("resource dir: $it")
            println("exists: ${it.exists()}")
        }
    }
}