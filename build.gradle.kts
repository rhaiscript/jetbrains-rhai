plugins {
    id("java")
    id("org.jetbrains.grammarkit") version "2022.3.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "org.rhai"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    apply {
        plugin("idea")
        plugin("kotlin")
        plugin("org.jetbrains.grammarkit")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
        maven("https://cache-redirector.jetbrains.com/repo.maven.apache.org/maven2")
        maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    }

    idea {
        module {
            generatedSourceDirs.add(file("src/main/gen"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

intellij {
    version.set("2023.3")
    type.set("IC")

//    plugins.set(listOf("org.intellij.intellij.grammarKit"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    generateLexer {
        sourceFile.set(project.file("src/main/grammars/RhaiLexer.flex"))
        targetDir.set("src/main/gen/org/rhai/")
        targetClass.set("RhaiLexer")
        purgeOldFiles.set(true)
    }

    generateParser {
        sourceFile.set(project.file("src/main/grammars/Rhai.bnf"))
        targetRoot.set("src/main/gen")
        pathToParser.set("org/rhai/RhaiParser.java")
        pathToPsiRoot.set("org/rhai")
        purgeOldFiles.set(true)
    }

    compileKotlin {
        dependsOn(generateLexer, generateParser)
    }

    clean {
        doLast {
            delete("src/main/gen")
        }
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        exclude("META-INF/plugin.xml")
        dependsOn(patchPluginXml)
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/gen")
        kotlin.srcDirs("src/main/kotlin")
        resources.srcDirs("src/main/resources")
    }
}