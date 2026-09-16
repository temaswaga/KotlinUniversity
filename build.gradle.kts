plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10" apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}