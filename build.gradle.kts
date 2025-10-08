plugins {
    id("com.diffplug.spotless") version "8.0.0"
    id("com.gradleup.shadow") version "9.2.2"
    kotlin("jvm") version "2.2.20"
    application
}

repositories { mavenCentral() }

application { mainClass = "MainKt" }

spotless {
    kotlin {
        target("*.gradle.kts", "src/**/*.kt")
        ktfmt().kotlinlangStyle()
        ktlint()
    }
    flexmark {
        target("*.md")
        flexmark()
        endWithNewline()
    }
}
