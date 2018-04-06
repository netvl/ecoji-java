import com.jfrog.bintray.gradle.Artifact
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.RecordingCopyTask
import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.io.PrintWriter
import java.util.Properties

group = "io.github.netvl.ecoji"
version = "1.0.0"

plugins {
    java
    idea
    `maven-publish`
    id("com.adarshr.test-logger").version("1.1.2")
    id("org.ajoberstar.git-publish").version("0.3.3")
    id("com.jfrog.bintray").version("1.7.3")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("com.google.guava", "guava", "24.1-jre")
    testImplementation("org.quicktheories", "quicktheories", "0.25")
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.1.0")
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", "5.1.0")
}

val generatedSourcesDir = File(buildDir, "generated/java")

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7

    sourceSets {
        "main" {
            java {
                srcDirs(generatedSourcesDir)
            }
        }
    }
}

idea {
    module {
        generatedSourceDirs.add(generatedSourcesDir)
    }
}

gitPublish {
    repoUri = if (System.getenv("CI") != null) {
        "https://github.com/netvl/ecoji-java.git"
    } else {
        "git@github.com:netvl/ecoji-java.git"
    }

    branch = "gh-pages"

    contents {
        from(tasks["javadoc"]) {
            into("api")
        }
    }
}

val tempRepo = "$buildDir/tempRepo"

publishing {
    (publications) {
        "mavenJava"(MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourceJar"])
            artifact(tasks["javadocJar"])

            pom.withXml {
                asNode().apply {
                    appendNode("name", "ecoji-java")
                    appendNode("description", "A Java implementation of the Ecoji encoding standard")
                    appendNode("url", "https://github.com/netvl/ecoji-java")
                    appendNode("licenses").apply {
                        appendNode("license").apply {
                            appendNode("name", "Apache 2.0")
                            appendNode("url", "https://raw.githubusercontent.com/netvl/ecoji-java/master/LICENSE")
                            appendNode("distribution", "repo")
                        }
                    }
                    appendNode("developers").apply {
                        appendNode("developer").apply {
                            appendNode("name", "Vladimir Matveev")
                            appendNode("email", "vladimir.matweev@gmail.com")
                            appendNode("url", "https://github.com/netvl")
                        }
                    }
                    appendNode("scm").apply {
                        appendNode("connection", "scm:git:https://github.com/netvl/ecoji-java")
                        appendNode("developerConnection", "scm:git:git@github.com:netvl/ecoji-java.git")
                        appendNode("url", "https://github.com/netvl/ecoji-java")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(tempRepo)
        }
    }
}

bintray {
    run {
        val propsPath = projectDir.toPath().resolve("bintray.properties")
        val props = Properties()
        Files.newBufferedReader(propsPath).use { props.load(it) }

        user = props.getProperty("bintray.user")
        key = props.getProperty("bintray.key")
        pkg.version.gpg.passphrase = props.getProperty("bintray.gpg.passphrase")
        pkg.version.mavenCentralSync.user = props.getProperty("sonatype.user")
        pkg.version.mavenCentralSync.password = props.getProperty("sonatype.password")
    }

    override = true
    publish = false

    setPublications("mavenJava")

    filesSpec(closureOf<RecordingCopyTask> {
        dependsOn(tasks["publish"])
        from(tempRepo) {
            // looks like bintray does not support uploading sha1, but including then just in case
            include("**/*.sha1")
            include("**/*.md5")
            exclude("**/maven-metadata.xml*")
        }
        into(".")
    })

    pkg.run {
        repo = "maven"
        name = "ecoji-java"
        version.run {
            name = project.version.toString()
            gpg.run {
                sign = true
            }
        }
    }
}

tasks {
    val generateEmojiMapping by creating {
        doLast {
            val input = Files.readAllLines(Paths.get("emojis.txt")).map { "0x$it" }.toMutableList()
            val outputPath = generatedSourcesDir.toPath()
                .resolve("io/github/netvl/ecoji")
                .resolve("Emojis.java")
            Files.createDirectories(outputPath.parent)

            Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
                val output = PrintWriter(it)
                output.println("package io.github.netvl.ecoji;")
                output.println("final class Emojis {")

                output.println("    private Emojis() {}")

                output.println("    static final int PADDING = 0x2615;")
                output.println("    static final int PADDING_40 = 0x269C;")
                output.println("    static final int PADDING_41 = ${input.removeAt(256)};")
                output.println("    static final int PADDING_42 = ${input.removeAt(512)};")
                output.println("    static final int PADDING_43 = ${input.removeAt(768)};")

                output.println("    static final int[] MAPPING = new int[] {")
                input.take(1024).forEachIndexed { i, s ->
                    output.println("        $s,  // $i")
                }
                output.println("    };")

                output.println("    static final java.util.Map<Integer, Integer> MAPPING_REV = new java.util.HashMap<>();")
                output.println("    static {")
                output.println("        for (int i = 0; i < MAPPING.length; ++i) {")
                output.println("            MAPPING_REV.put(MAPPING[i], i);")
                output.println("        }")
                output.println("    }")
                output.println("    static int revMapGetOrZero(int k) {")
                output.println("        Integer value = MAPPING_REV.get(k);")
                output.println("        if (value == null) {")
                output.println("            return 0;")
                output.println("        } else {")
                output.println("            return value;")
                output.println("        }")
                output.println("    }")

                output.println("    static boolean isValidAlphabetChar(int c) {")
                output.println("        return c == PADDING || c == PADDING_40 || c == PADDING_41 ||")
                output.println("               c == PADDING_42 || c == PADDING_43 || MAPPING_REV.containsKey(c);")
                output.println("    }")


                output.println("}")
            }
        }
    }

    "test"(Test::class) {
        useJUnitPlatform()
    }

    "compileJava" {
        dependsOn(generateEmojiMapping)
    }

    "compileTestJava"(JavaCompile::class) {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    "gitPublishCopy" {
        dependsOn("javadoc"())
    }

    "sourceJar"(Jar::class) {
        classifier = "sources"
        from(java.sourceSets["main"].allJava)
    }

    "javadocJar"(Jar::class) {
        classifier = "javadoc"
        from("javadoc"(Javadoc::class).destinationDir)
        dependsOn("javadoc"())
    }
}
