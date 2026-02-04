plugins {
    id("java")
    id("org.jetbrains.grammarkit") version "2022.3.1"
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.16.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

group = "org.rhai"
version = "0.0.2"

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
}

detekt {
    config.setFrom(file("detekt.yml"))
    buildUponDefaultConfig = true
    source.setFrom(
        "src/main/kotlin"
    )
}

tasks {
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

    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
            sarif.required.set(false)
        }
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("251.*")
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
    test {
        kotlin.srcDirs("src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

tasks {
    test {
        useJUnit()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
