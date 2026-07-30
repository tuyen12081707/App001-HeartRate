import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class CopySharedComposeResourcesToJavaResources : DefaultTask() {
    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:InputDirectory
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun copyResources() {
        fileSystemOperations.copy {
            from(inputDirectory)
            into(
                outputDirectory.dir(
                    "composeResources/app001heartrate.shared.generated.resources"
                )
            )
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

val copySharedComposeResourcesToJavaResources by tasks.registering(
    CopySharedComposeResourcesToJavaResources::class
) {
    dependsOn(project(":shared").tasks.named("prepareComposeResourcesTaskForCommonMain"))
    inputDirectory.set(
        project(":shared").layout.buildDirectory.dir(
            "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
        )
    )
    outputDirectory.set(layout.buildDirectory.dir("generated/sharedComposeJavaResources"))
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.resources?.addGeneratedSourceDirectory(
            copySharedComposeResourcesToJavaResources,
            CopySharedComposeResourcesToJavaResources::outputDirectory
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.core)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.tdev.heartrate"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.tdev.heartrate"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}
