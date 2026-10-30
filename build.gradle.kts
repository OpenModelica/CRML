import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.0"
}

group = "crml"
version = "1.0-SNAPSHOT"

// ─────────────────────────────────────────────
//  Shared version catalogue
// ─────────────────────────────────────────────
val emfCoreVersion    by extra("2.33.0")   // org.eclipse.emf.ecore + codegen in xtextLanguages
val emfCommonVersion  by extra("2.28.0")   // org.eclipse.emf.common
val emfCodegenVersion by extra("2.45.0")   // org.eclipse.emf.codegen.ecore (generateGenModel only)
val xcoreVersion      by extra("1.21.0")   // org.eclipse.emf.ecore.xcore
val xcoreLibVersion   by extra("1.6.0")    // org.eclipse.emf.ecore.xcore.lib
val xtextVersion      by extra("2.33.0")   // org.eclipse.xtext.* (build-time Xcore generator only)
val xbaseLibVersion   by extra("2.25.0")   // org.eclipse.xtext.xbase.lib at runtime; last release built for Java 8
val antlr4Version     by extra("4.9.2")    // org.antlr:antlr4

allprojects {
    repositories {
        mavenCentral()
       /* maven {
            url = uri("https://repo.eclipse.org/content/repositories/viatra2-releases/")
        }
        maven {
            url = uri("https://repo.eclipse.org/content/groups/releases/")
        }*/
    }
}

// The GitHub Packages repository that hosts emf-mermaid is declared in
// :model, the one project that can use it, and only the optional
// generateMermaid task resolves anything from it. Building the project needs
// no GitHub credentials.

subprojects {
    plugins.apply("java")

    /*
    //Toolchan for gradle only, probably not necessary.
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }*/
}

// Compile with any JDK that can run Gradle (17+), but emit Java 8 bytecode
// against the Java 8 API, since the compiler must run on a Java 8 JRE.
// allprojects, so the root project's java plugin is held to 8 as well.
allprojects {
    plugins.withType<JavaPlugin> {
        tasks.withType<JavaCompile>().configureEach {
            options.release.set(8)
        }
        // Much of the API is EMF-generated: skip its @generated/@model tags and
        // don't fail on the missing @param/@return that doclint reports for it.
        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).apply {
                tags("generated:X", "model:X", "ordered:X")
                addStringOption("Xdoclint:none", "-quiet")
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Javadoc of all the projects in one site
// ─────────────────────────────────────────────
// build/docs/javadoc, from the sources and classpaths of each project's own
// javadoc task. Of :web, its org.* and crml.web.emul packages are left out:
// they are stand-ins for Eclipse/OSGi/log4j/javax.xml, which TeaVM compiles
// instead of the real ones.
val javadocAll by tasks.registering(Javadoc::class) {
    group = "documentation"
    description = "Generates the Javadoc of all the projects into build/docs/javadoc."
    title = "CRML compiler API"
    setDestinationDir(layout.buildDirectory.dir("docs/javadoc").get().asFile)
    // The site contains only what is generated now, not leftovers of removed classes.
    doFirst { delete(destinationDir) }
}
subprojects {
    plugins.withType<JavaPlugin> {
        val javadoc = tasks.named<Javadoc>("javadoc")
        val web = name == "web"
        javadocAll.configure {
            source(javadoc.map { if (web) it.source.matching { include("crml/**"); exclude("crml/web/emul/**") } else it.source })
            classpath += files(javadoc.map { it.classpath })
        }
    }
}

// Serves the site above with the JDK's HTTP server, so nothing is downloaded,
// until the build is stopped with Ctrl+C:
//   ./gradlew serveJavadoc [-Pport=8001]
tasks.register("serveJavadoc") {
    group = "documentation"
    description = "Serves the Javadoc on http://localhost:<port>/ (-Pport, default 8001) until stopped."
    dependsOn(javadocAll)
    val port = (project.findProperty("port") ?: "8001").toString().toInt()
    val root = javadocAll.map { it.destinationDir!!.toPath().toAbsolutePath().normalize() }
    doLast {
        val dir = root.get()
        val types = mapOf(
            "html" to "text/html; charset=utf-8",
            "css" to "text/css; charset=utf-8",
            "js" to "text/javascript; charset=utf-8",
            "json" to "application/json; charset=utf-8",
            "png" to "image/png",
            "svg" to "image/svg+xml",
            "zip" to "application/zip"
        )
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.createContext("/") { exchange ->
            exchange.use {
                var file = dir.resolve(it.requestURI.path.removePrefix("/")).normalize()
                if (Files.isDirectory(file)) file = file.resolve("index.html")
                if (!file.startsWith(dir) || !Files.isRegularFile(file)) {
                    it.sendResponseHeaders(404, -1)
                } else {
                    val bytes = Files.readAllBytes(file)
                    it.responseHeaders.add("Content-Type",
                        types[file.fileName.toString().substringAfterLast('.')] ?: "application/octet-stream")
                    // So that a page reloaded after a rebuild shows the new docs.
                    it.responseHeaders.add("Cache-Control", "no-store")
                    it.sendResponseHeaders(200, bytes.size.toLong())
                    it.responseBody.write(bytes)
                }
            }
        }
        server.start()
        logger.lifecycle("Serving $dir on http://localhost:$port/ — stop with Ctrl+C")
        try {
            Thread.currentThread().join()
        } finally {
            server.stop(0)
        }
    }
}
