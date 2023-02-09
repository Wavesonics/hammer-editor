val app_version: String by extra
val android_compile_sdk: String by extra
val android_target_sdk: String by extra
val android_min_sdk: String by extra
val kotlin_version: String by extra
val coroutines_version: String by extra
val kotlinx_serialization_version: String by extra
val decompose_version: String by extra
val koin_version: String by extra
val okio_version: String by extra
val essenty_version: String by extra
val mockk_version: String by extra

plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
	id("com.android.library")
	id("kotlin-parcelize")
	id("org.jetbrains.kotlinx.kover")
}

group = "com.darkrockstudios.apps.hammer"
version = app_version

kotlin {
	android()
	jvm("desktop") {
		compilations.all {
			kotlinOptions.jvmTarget = "11"
		}
	}
	ios {
		binaries {
			framework {
				baseName = "Hammer"
				//transitiveExport = true
				export("com.arkivanov.decompose:decompose:$decompose_version")
				// This isn't working for some reason, once it is remove transitiveExport
				export("com.arkivanov.essenty:lifecycle:$essenty_version")
				export("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
			}
		}
	}

	sourceSets {
		val commonMain by getting {
			resources.srcDirs("resources")

			dependencies {
				api("com.arkivanov.decompose:decompose:$decompose_version")
				api("io.github.aakira:napier:2.6.1")
				api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
				api("io.insert-koin:koin-core:$koin_version")
				api("com.squareup.okio:okio:$okio_version")

				api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinx_serialization_version")
				// This is being held back to 0.3.2 due to ios support not working in later versions
				api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
				implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
				implementation("com.akuleshov7:ktoml-core:0.4.1")
				api("com.arkivanov.essenty:lifecycle:$essenty_version")
				implementation("io.github.reactivecircus.cache4k:cache4k:0.9.0")
			}
		}
		val commonTest by getting {
			dependencies {
				implementation(kotlin("test"))
				//implementation("io.insert-koin:koin-test:$koin_version")
				implementation("com.squareup.okio:okio-fakefilesystem:$okio_version")
				implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")
			}
		}
		val androidMain by getting {
			dependencies {
				//api("androidx.appcompat:appcompat:1.5.1")
				api("androidx.core:core-ktx:1.9.0")
				api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
				api("io.insert-koin:koin-android:$koin_version")
			}
		}
		val iosMain by getting {
			dependencies {
				api("com.arkivanov.decompose:decompose:$decompose_version")
				api("com.arkivanov.essenty:lifecycle:$essenty_version")
			}
		}
		val iosTest by getting
		val androidUnitTest by getting {
			dependencies {
			}
		}
		val desktopMain by getting {
			dependencies {
				api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$kotlinx_serialization_version")
				api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$coroutines_version")
				implementation("net.harawata:appdirs:1.2.1")
			}
		}
		val desktopTest by getting {
			dependencies {
				implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
				implementation("io.mockk:mockk:$mockk_version")
				implementation("io.insert-koin:koin-test:$koin_version")
			}
		}
	}
}

android {
	namespace = "com.darkrockstudios.apps.hammer.common"
	compileSdk = android_compile_sdk.toInt()
	sourceSets {
		named("main") {
			manifest.srcFile("src/androidMain/AndroidManifest.xml")
			res.srcDirs("resources", "src/androidMain/res", "src/commonMain/resources")
		}
	}
	defaultConfig {
		minSdk = android_min_sdk.toInt()
		targetSdk = android_target_sdk.toInt()
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
}

kover {
	filters {
		classes {
			includes += "com.darkrockstudios.apps.hammer.*"
		}
	}
}