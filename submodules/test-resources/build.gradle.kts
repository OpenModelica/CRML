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

tasks.register("debugResources") {
    doLast {
        sourceSets["main"].resources.srcDirs.forEach {
            println("resource dir: $it")
            println("exists: ${it.exists()}")
        }
    }
}