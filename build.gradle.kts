import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentFilter
import com.github.benmanes.gradle.versions.updates.resolutionstrategy.ComponentSelectionWithCurrent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.inputStream


application {
    mainClass.set("com.github.ekenstein.sgf2gif.MainKt")
}

plugins {
    application
    kotlin("jvm") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "12.0.3"
    id("com.github.ben-manes.versions") version "0.50.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.ekenstein"
version = "0.4.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.Ekenstein", "haengma", "2.2.6")
    implementation("org.jetbrains.kotlinx", "kotlinx-cli", "0.3.6")
    testImplementation(kotlin("test"))
}

tasks {
    shadowJar {
        minimize()
    }
    register<Copy>("packageDistribution") {
        dependsOn("shadowJar")
        from("${project.rootDir}/scripts/${project.name}")

        from("${project.projectDir}/build/libs/${project.name}-${project.version}-all.jar") {
            into("lib")
        }

        into("${project.rootDir}/dist")
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes["Main-Class"] = application.mainClass
        }

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        archiveFileName.set("${project.name}.jar")
    }

    dependencyUpdates {
        rejectVersionIf {
            UpgradeToUnstableFilter().reject(this) || IgnoredDependencyFilter().reject(this)
        }
    }
    val dependencyUpdateSentinel = register<DependencyUpdateSentinel>("dependencyUpdateSentinel", buildDir)
    dependencyUpdateSentinel.configure {
        dependsOn(dependencyUpdates)
    }

    test {
        useJUnitPlatform()
    }

    check {
        dependsOn(test)
        dependsOn(ktlintCheck)
        dependsOn(dependencyUpdateSentinel)
    }

    withType<JavaCompile> {
        targetCompatibility = "1.8"
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

ktlint {
    version.set("0.45.2")
}

class UpgradeToUnstableFilter : ComponentFilter {
    override fun reject(candidate: ComponentSelectionWithCurrent) = reject(
        candidate.currentVersion,
        candidate.candidate.version
    )

    private fun reject(old: String, new: String): Boolean {
        return !isStable(new) && isStable(old) // no unstable proposals for stable dependencies
    }

    private fun isStable(version: String): Boolean {
        val stableKeyword = setOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val stablePattern = version.matches(Regex("""^[0-9,.v-]+(-r)?$"""))
        return stableKeyword || stablePattern
    }
}

class IgnoredDependencyFilter : ComponentFilter {
    private val ignoredDependencies = mapOf(
        "ktlint" to listOf(
            "0.46.0",
            "0.46.1",
            "0.47.0",
            "0.47.1",
            "0.48.0",
            "0.48.1",
            "0.48.2",
            "0.49.0"
        ) // doesn't currently work.
    )

    override fun reject(candidate: ComponentSelectionWithCurrent): Boolean {
        return ignoredDependencies[candidate.candidate.module].orEmpty().contains(candidate.candidate.version)
    }
}

abstract class DependencyUpdateSentinel @Inject constructor(private val buildDir: File) : DefaultTask() {
    @ExperimentalPathApi
    @TaskAction
    fun check() {
        val updateIndicator = "The following dependencies have later milestone versions:"
        val report = Paths.get(buildDir.toString(), "dependencyUpdates", "report.txt")

        report.inputStream().bufferedReader().use { reader ->
            if (reader.lines().anyMatch { it == updateIndicator }) {
                throw GradleException("Dependency updates are available.")
            }
        }
    }
}
