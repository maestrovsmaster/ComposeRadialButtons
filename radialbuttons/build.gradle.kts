plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "com.radialbuttons"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Core Android
    implementation(libs.androidx.core.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Publishing configuration for JitPack
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                // Use JitPack properties or fallback to defaults
                groupId = project.findProperty("group")?.toString() ?: "com.github.maestrovsmaster"
                artifactId = "compose-radial-buttons"
                version = project.findProperty("version")?.toString() ?: "1.0.0"

                pom {
                    name.set("Compose Radial Buttons")
                    description.set("A circular/radial button layout library for Jetpack Compose with customizable buttons, animations, and touch detection")
                    url.set("https://github.com/maestrovsmaster/ComposeRadialButtons")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("maestrovsmaster")
                            name.set("MaestroCreations")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/maestrovsmaster/ComposeRadialButtons.git")
                        developerConnection.set("scm:git:ssh://github.com/maestrovsmaster/ComposeRadialButtons.git")
                        url.set("https://github.com/maestrovsmaster/ComposeRadialButtons")
                    }
                }
            }
        }
    }
}
