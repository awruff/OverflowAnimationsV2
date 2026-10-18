plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.loom)
    alias(libs.plugins.ploceus)
}

val minecraftVersion = libs.versions.minecraft.get()

base.archivesName = "overflowanimations"
version = "$version+$minecraftVersion-ornithe"

repositories {
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://maven.cloverclient.com/releases")
    maven("https://maven.taumc.org/releases")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

configurations.configureEach {
    exclude(group = "org.lwjgl.lwjgl")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(ploceus.featherMappings(libs.versions.feather.get()))
    modImplementation(libs.fabric.loader)
    ploceus.dependOsl(libs.versions.osl.get())

    modImplementation(libs.oneconfig.ornithe)
    implementation(libs.bundles.oneconfig)
    modImplementation(libs.fabric.language.kotlin)

    modCompileOnly(libs.overflowparticles) { isTransitive = false }
    modCompileOnly(libs.argentum)
    modLocalRuntime(libs.argentum)
}

loom {
    runConfigs.remove(runConfigs["server"])
}

kotlin {
    jvmToolchain(25)
}

tasks.processResources {
    val props = mapOf("version" to version, "minecraft" to minecraftVersion, "loader" to libs.versions.fabric.loader.get())
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

tasks.jar {
    from("LICENSE") { rename { "${it}_overflowanimations" } }
}
