import com.gtnewhorizons.retrofuturagradle.modutils.ModUtils

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
        maven {
            // GTNH RetroFuturaGradle and ASM Fork
            name = "GTNH Maven"
            url = uri("http://jenkins.usrv.eu:8081/nexus/content/groups/public/")
            isAllowInsecureProtocol = true
        }
    }
}

plugins {
    id("java-library")
    // ForgeGradle
    id("com.gtnewhorizons.retrofuturagradle") version "1.4.1" apply true
}

tasks.compileJava {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    sourceCompatibility = "1.8"
}

configurations.all {
    afterEvaluate {
        attributes {
            attribute(ModUtils.DEOBFUSCATOR_TRANSFORMED, true)
        }
    }
}

