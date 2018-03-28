import org.ajoberstar.gradle.git.publish.GitPublishExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.io.PrintWriter

group = "io.github.netvl.ecoji"
version = "1.0.0"

plugins {
    java
    idea
    id("com.adarshr.test-logger").version("1.1.2")
    id("org.ajoberstar.git-publish").version("0.3.3")
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

configure<JavaPluginConvention> {
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

configure<IdeaModel> {
    module {
        generatedSourceDirs.add(generatedSourcesDir)
    }
}

configure<GitPublishExtension> {
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
        dependsOn(tasks["javadoc"])
    }
}
