import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("com.google.devtools.ksp") version "2.3.6"
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("ploceus") version "1.17.4"
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22"
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    id("com.diffplug.spotless") version "8.2.1"
}

fun prop(name: String): String = property(name).toString()

val modid = prop("mod.id")
val modversion = prop("mod.version")
val mcversion = prop("deps.minecraft")
val loaderversion = prop("deps.fabric_loader")
val javaVersion = JavaVersion.VERSION_25

version = "$modversion+$mcversion-ornithe"
group = prop("mod.group")
base.archivesName = modid

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

val ploceus = extensions.getByType<PloceusGradleExtensionApi>().apply {
    setIntermediaryGeneration(2)
}

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenCentral()
    strictMaven("https://maven.fabricmc.net/", "FabricMC", "net.fabricmc")
    google()
    maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
    maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
    strictMaven("https://maven.cloverclient.com/releases", "CloverClient", "pl.tomgirl")
    strictMaven("https://maven.taumc.org/releases", "TauMC", "dev.rdh", "org.embeddedt.celeritas")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    strictMaven("https://maven.pkg.jetbrains.space/public/p/compose/dev", "JetBrains Compose Dev", "org.jetbrains.skiko")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcversion")
    mappings(ploceus.layeredMappings {
        mappings("net.ornithemc:feather-gen2:$mcversion+build.${prop("deps.feather")}:v2") {
            containsUnpick()
        }
    })
    ploceus.dependOsl(prop("deps.osl"))

    modImplementation("net.fabricmc:fabric-loader:$loaderversion")
    val oneconfig = prop("deps.oneconfig")
    modImplementation("org.polyfrost.oneconfig:$mcversion-ornithe:$oneconfig")
    for (module in arrayOf("config", "config-impl", "internal", "ui", "utils")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfig")
    }
    modImplementation("net.fabricmc:fabric-language-kotlin:${prop("deps.fabric_language_kotlin")}")

    modCompileOnly("maven.modrinth:overflowparticles:${prop("deps.overflowparticles")}") { isTransitive = false }
    modCompileOnly("dev.rdh:argentum:${prop("deps.argentum")}") { isTransitive = false }
    compileOnly("org.joml:joml:1.10.5")
    modLocalRuntime("dev.rdh:argentum:${prop("deps.argentum")}") { isTransitive = false }
    localRuntime("org.embeddedt.celeritas:celeritas-common:${prop("deps.celeritas")}") // jar-in-jar isn't loaded from dev deps
    localRuntime("org.joml:joml:1.10.5")
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
        jvmArguments.add("-Dmixin.hotSwap=true")
        if (System.getProperty("os.name").startsWith("Mac")) jvmArguments.add("-XstartOnFirstThread")
    }

    runConfigs.remove(runConfigs["server"])
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "$modid.mixins.json") {
            env("CLIENT")
        }
    }

    lang.create("main") {
        patterns.add("assets/$modid/lang/**")
    }
}

java {
    withSourcesJar()
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(javaVersion.majorVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion.majorVersion.toInt()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget = JvmTarget.fromTarget(javaVersion.majorVersion)
}

bloom {
    replacement("@MODID@", modid)
}

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    java {
        licenseHeaderFile(file("HEADER"))
        target("src/**/*.java")
    }

    kotlin {
        licenseHeaderFile(file("HEADER"))
        target("src/**/*.kt")
    }
}

tasks {
    processResources {
        val props = mapOf(
            "id" to modid,
            "name" to prop("mod.name"),
            "version" to modversion,
            "description" to prop("mod.description"),
            "source" to prop("mod.source"),
            "issues" to prop("mod.issues"),
            "license" to prop("mod.license"),
            "fabric_loader_version" to loaderversion,
            "minecraft_version" to mcversion
        )

        inputs.properties(props)
        filesMatching("fabric.mod.json") { expand(props) }
    }

    jar {
        inputs.property("archivesName", base.archivesName)

        from(file("LICENSE")) {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }
}

val modrinthId = findProperty("publish.modrinth")?.toString()?.takeIf { it.isNotBlank() }

// Tokens go in the user Gradle gradle.properties file (e.g. ~/.gradle/gradle.properties):
// modrinth.token=
// discord.webhook=
publishMods {
    file = tasks.remapJar.flatMap { it.archiveFile }
    displayName = "Release $modversion for $mcversion"
    version = modversion
    changelog = file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."
    type = STABLE
    modLoaders.add("ornithe")

    dryRun = modrinthId == null
    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = findProperty("modrinth.token").toString()
            minecraftVersions.add(mcversion)

            requires("osl")
            requires("oneconfig")
            requires("fabric-language-kotlin")
        }
    }

    findProperty("discord.webhook")?.toString()?.takeIf { it.isNotBlank() }?.let { url ->
        discord {
            webhookUrl = url
        }
    }
}
