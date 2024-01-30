// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // Safe Args
    id("androidx.navigation.safeargs.kotlin") version "2.6.0" apply false

    // KSP
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false

    // Room
    id("androidx.room") version "2.6.0" apply false

}