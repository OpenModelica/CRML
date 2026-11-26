import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files

// The group and version of every project, and so of the Maven publications.
// The version is -PcrmlVersion, set by .github/workflows/maven-publish.yml;
// locally it is a snapshot:
//   ./gradlew publish -PcrmlVersion=0.0.1
allprojects {
    group = "org.openmodelica.crml"
    version = (findProperty("crmlVersion") as String?)?.takeIf { it.isNotBlank() } ?: "0.0.1-SNAPSHOT"
}

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
val slf4jVersion      by extra("2.0.17")   // org.slf4j:slf4j-api, the logging API (Java 8); see Logging below
val log4jVersion      by extra("2.24.3")   // Log4j 2, the logging backend of Gradle's runs and tests only

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

// ─────────────────────────────────────────────
//  Logging
// ─────────────────────────────────────────────
// The code logs through the SLF4J API, which every Java project gets. No
// project packages a logging backend: not the jars, compiler-all.jar, the
// distributions or the Maven packages, so an application using them chooses
// its own (without one, SLF4J drops the messages). What Gradle runs logs
// through Log4j 2, configured by logging/log4j2.properties: the tests of every
// project, and the tasks that add the loggingBackend configuration to their
// classpath, such as run and crmlc in :compiler.
//
// :web is left out: TeaVM compiles it to JavaScript, where SLF4J cannot look
// up a backend; the sources it compiles do not log.
configure(subprojects.filter { it.name != "web" }) {
    val loggingBackend by configurations.creating {
        description = "The logging backend of Gradle's runs and tests: Log4j 2 behind SLF4J. Never packaged."
        isCanBeConsumed = false
    }
    configurations.named("testRuntimeOnly") {
        extendsFrom(loggingBackend)
    }
    dependencies {
        "implementation"("org.slf4j:slf4j-api:$slf4jVersion")
        // The API too, so that the backend never brings a version of its own.
        loggingBackend("org.slf4j:slf4j-api:$slf4jVersion")
        // The bridge for SLF4J 2.x; log4j-slf4j-impl is the one for 1.7.
        loggingBackend("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
        loggingBackend("org.apache.logging.log4j:log4j-core:$log4jVersion")
        loggingBackend(files(rootProject.file("logging")))
    }
}

// Kept off every classpath Gradle resolves, whatever brings them:
// log4j-slf4j-impl, the bridge for SLF4J 1.7, which SLF4J 2.x does not detect,
// so logging would silently do nothing; and log4j-to-slf4j, which sends Log4j
// to SLF4J, and with log4j-slf4j2-impl would pass every message back and
// forth between the two. Only on the classpaths: an exclude on the consumable
// configurations would be published.
subprojects {
    configurations.matching { it.name.endsWith("Classpath") || it.name == "loggingBackend" }.configureEach {
        exclude(group = "org.apache.logging.log4j", module = "log4j-slf4j-impl")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
}

// Compile with any JDK that can run Gradle (17+), but emit Java 8 bytecode
// against the Java 8 API, since the compiler must run on a Java 8 JRE.
// allprojects, so that javadocAll in the root project gets the same options.
allprojects {
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

// Every jar, compiler-all.jar included, carries the CRML license, and so does
// every distribution archive of the application projects.
subprojects {
    tasks.withType<Jar>().configureEach {
        // The jars keep names without the version (compiler-all.jar, ...),
        // which the Makefile and workflows refer to. Publishing names them.
        archiveVersion.set("")
        metaInf {
            from(rootProject.file("OSMC-License.txt"))
            rename("OSMC-License.txt", "LICENSE")
        }
    }
    plugins.withType<DistributionPlugin> {
        extensions.getByType<DistributionContainer>().configureEach {
            contents {
                from(rootProject.file("OSMC-License.txt")) { rename { "LICENSE" } }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  Maven publication (GitHub Packages)
// ─────────────────────────────────────────────
// The library projects are published as org.openmodelica.crml:crml-<project>,
// each with its sources and Javadoc; crml-compiler also carries compiler-all.jar
// (classifier "all"), with the group and version declared at the top.
// The repository is the GitHub Packages one of $GITHUB_REPOSITORY, with the
// gpr.user/gpr.key properties or GITHUB_ACTOR/GITHUB_TOKEN as credentials;
// .github/workflows/maven-publish.yml publishes there.
val publishedProjects = setOf("util", "model", "modelica", "language", "compiler")
val githubRepository = System.getenv("GITHUB_REPOSITORY") ?: "PELAB-LiU/CRML"

configure(subprojects.filter { it.name in publishedProjects }) {
    apply(plugin = "maven-publish")
    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }
    // Some projects name a source directory twice (src/main/java, the ANTLR
    // output), which would put its files twice into the sources jar.
    tasks.named<Jar>("sourcesJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                artifactId = "crml-${project.name}"
                from(components["java"])
                pom {
                    name.set("CRML ${project.name}")
                    description.set("The ${project.name} project of the CRML compiler")
                    url.set("https://github.com/$githubRepository")
                    licenses {
                        license {
                            name.set("OSMC Public License")
                            url.set("https://github.com/$githubRepository/blob/main/OSMC-License.txt")
                        }
                    }
                    scm {
                        url.set("https://github.com/$githubRepository")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/$githubRepository")
                credentials {
                    username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                    password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
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
