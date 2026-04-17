plugins {
    java
    // Epsilon EMF support
    id("org.epsilonlabs.emf")
}

repositories {
    mavenCentral()
}

dependencies {

    // Add required EMF dependencies
    implementation("org.eclipse.emf:org.eclipse.emf.ecore:2.34.0")
    implementation("org.eclipse.emf:org.eclipse.emf.ecore.xmi:2.18.0")
    implementation("org.eclipse.emf:org.eclipse.emf.codegen.ecore:2.35.0")
}

// 1. Task to convert Emfatic to Ecore
tasks.register<org.epsilonlabs.emf.gradle.EmfaticToEcore>("emfaticToEcore") {
    source.set(file("model/myModel.emf"))
    output.set(file("model/myModel.ecore"))
}

// 2. Task to generate Java code from Ecore
tasks.register<org.epsilonlabs.emf.gradle.EcoreToGenModel>("generateEMFCode") {
    dependsOn("emfaticToEcore")

    ecoreFile.set(file("model/myModel.ecore"))
    genModelFile.set(file("model/myModel.genmodel"))
    basePackage.set("com.example.myproject")
    generateCode.set(true)
}

// Ensure the code is generated before compiling
tasks.named<JavaCompile>("compileJava") {
    dependsOn("generateEMFCode")
}