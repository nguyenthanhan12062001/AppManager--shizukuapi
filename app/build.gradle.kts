plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.shizukumanager.app"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.shizukumanager.kzmpxq"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      def signingPropsFile = rootProject.file("signing.properties")
      def props = new Properties()
      if (signingPropsFile.exists()) {
        signingPropsFile.withInputStream { props.load(it) }
      }
      def storeFileProp = props.getProperty("storeFile", "")
      def storePasswordProp = props.getProperty("storePassword", "")
      def keyAliasProp = props.getProperty("keyAlias", "")
      def keyPasswordProp = props.getProperty("keyPassword", "")

      storeFile = file(storeFileProp)
      storePassword = storePasswordProp
      keyAlias = keyAliasProp
      keyPassword = keyPasswordProp
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }

  // Fail Release builds with a clear message when signing.properties is missing or incomplete.
  android.libraryVariants.all { variant ->
    if (variant.buildType.name == "release") {
      variant.registerResGeneratingTask("validateReleaseSigning")
    }
  }
  tasks.register("validateReleaseSigning") {
    group = "verification"
    description = "Validates that release signing.properties exists before building Release."
    doLast {
      def propsFile = rootProject.file("signing.properties")
      if (!propsFile.exists()) {
        throw new GradleException(
          "Release signing configuration missing. " +
          "Create signing.properties from signing.properties.example and fill in your own keystore values."
        )
      }
      def props = new Properties()
      propsFile.withInputStream { props.load(it) }
      def required = ["storeFile", "storePassword", "keyAlias", "keyPassword"]
      def missing = required.filter { !props.getProperty(it) }
      if (!missing.isEmpty()) {
        throw new GradleException(
          "Release signing.properties is incomplete. Missing: ${missing.join(", ")}. " +
          "Edit signing.properties with your own keystore values."
        )
      }
    }
  }

  tasks.named("assembleRelease") {
    dependsOn("validateReleaseSigning")
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    viewBinding = true
    aidl = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

// Unused Compose/Firebase/Networking template dependencies are commented out for APK optimization.
dependencies {
  implementation(libs.androidx.core.java)
  implementation(libs.androidx.appcompat)
  implementation(libs.google.material)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.swiperefreshlayout)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.livedata)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.room.runtime)
  annotationProcessor(libs.androidx.room.compiler)
  implementation(libs.shizuku.api)
  implementation(libs.shizuku.provider)
  implementation(libs.gson)

  // Unused template dependencies commented out:
  // implementation(platform(libs.androidx.compose.bom))
  // implementation(platform(libs.firebase.bom))
  // implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.compose.material3)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.firebase.ai)

  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
}
