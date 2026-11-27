plugins {
    id("java")
}

dependencies {
    implementation(project(":util"))
}

/**
 * Packages the CRML Modelica library (libraries/modelica/CRML) as the
 * resource folder modelica_libraries/CRML. The compiler extracts it from
 * the classpath to load it when simulating verification models.
 */
val packageCRMLLibrary by tasks.registering(Sync::class) {
    description = "Packages the CRML Modelica library into the test resources."
    from(rootProject.layout.projectDirectory.dir("libraries/modelica/CRML")) {
        into("modelica_libraries/CRML")
    }
    into(layout.buildDirectory.dir("generated/crml-library"))
}

sourceSets {
    main {
        resources {
            // Appends an external directory to the build reources
            srcDir("${rootProject.projectDir}/resources/crml_tutorial")
            // The packaged CRML library (adds a dependency on packageCRMLLibrary)
            srcDir(packageCRMLLibrary)
        }
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