// Top-level build file where you can add configuration options common to all sub-projects/modules.
ext {
    extra["compose_version"] = "1.1.1"
}

plugins {
    /* Version 7.0.4 is not the latest. It is required, because Intellij doesn't support any newer. */
    id("com.android.application") version "7.0.4" apply false
    id("com.android.library") version "7.0.4" apply false
    /* Version 1.6.10 is not the latest. It is required by compose version 1.1.1 */
    id("org.jetbrains.kotlin.android") version "1.6.20" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}