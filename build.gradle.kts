import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

plugins {
    id("fabric-loom") version "1.17-SNAPSHOT"
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

repositories {
    mavenCentral()
}

val identityMappings: File = layout.projectDirectory.file("gradle/identity-mappings.jar").asFile.apply {
    parentFile.mkdirs()
    if (!exists()) {
        ZipOutputStream(outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("mappings/mappings.tiny"))
            zip.write("tiny\t2\t0\tofficial\tintermediary\tnamed\n".toByteArray())
            zip.closeEntry()
        }
    }
}

loom {
    runs.configureEach {
        property("fabric.runtimeMappingNamespace", "named")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(files(identityMappings))

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.processResources {
    val modVersion = project.version.toString()
    val loaderVersion = project.property("loader_version").toString()
    val minecraftVersion = project.property("minecraft_version").toString()

    inputs.property("version", modVersion)
    inputs.property("loader_version", loaderVersion)
    inputs.property("minecraft_version", minecraftVersion)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to modVersion,
            "loader_version" to loaderVersion,
            "minecraft_version" to minecraftVersion,
        )
    }
}
