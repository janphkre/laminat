import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
// FIXME: THROWS ERROR?!
//    //select iOS target platform depending on the Xcode environment variables
//    val iOSTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
//    if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true) {
//        :: iosArm64
//    } else {
//        :: iosX64
//    }
    iosArm64("ios") {
        binaries {
            framework {
                baseName = "SharedCode"
            }
        }
    }

    jvm("android")

    repositories {
        mavenCentral()
        jcenter()
    }

    sourceSets["commonMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${properties["mainSerializationVersion"]}")
//        implementation("com.google.code.gson:gson:${properties["mainGsonVersion"]}") // TODO: JSON ALTERNATIVE IN KOTLIN NATIVE? (kotlinx serialization)
//        implementation("org.apache.commons:commons-lang3:${properties["mainApacheCommonsVersion"]}") // TODO: COMMONS NECESSARY FOR LISTS?
//        implementation("com.github.mifmif:generex:${properties["mainGenerexVersion"]}") // TODO: DROP SUPPORT FOR REGEX ITEM GENERATION?
//        implementation("io.gatling:jsonpath_2.11:${properties["mainJsonpathVersion"]}") // TODO: WHAT DOES THIS DO? CAN IT BE REMOVED?

        // The Android vm does not like the library "org.apache.httpcomponents:httpcore:4.4.4"
        // because it already contains parts from it that are final for example but a super class in the lib.
        // The required java classes are added manually into org.apache.http...
    }

    sourceSets["androidMain"].dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:${properties["mainSerializationVersion"]}")

        implementation("com.squareup.okhttp3:mockwebserver:${properties["androidMockServerVersion"]}") // TODO: HOW TO SPECIFY A VERSION?

        // TODO: MAKE TESTS WORK AGAIN
        //testImplementation "junit:junit:$test_junit_version"
        //testImplementation "org.json:json:$test_json_version"
        //testImplementation "org.mockito:mockito-core:$test_mockito_version"
    }

    sourceSets["iosMain"].dependencies {
//        implementation("org.jetbrains.kotlin:kotlin-stdlib-native") //TODO??
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:${properties["mainSerializationVersion"]}")
    }
}

val packForXcode by tasks.creating(Sync::class) {
    val targetDir = File(buildDir, "xcode-frameworks")

    /// selecting the right configuration for the iOS
    /// framework depending on the environment
    /// variables set by Xcode build
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets
        .getByName<KotlinNativeTarget>("ios")
        .binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)

    from({ framework.outputDirectory })
    into(targetDir)

    /// generate a helpful ./gradlew wrapper with embedded Java path
    doLast {
        val gradlew = File(targetDir, "gradlew")
        gradlew.writeText("#!/bin/bash\n" +
            "export 'JAVA_HOME=${System.getProperty("java.home")}'\n" +
            "cd '${rootProject.rootDir}'\n" +
            "./gradlew \$@\n")
        gradlew.setExecutable(true)
    }
}

tasks.getByName("build").dependsOn(packForXcode)