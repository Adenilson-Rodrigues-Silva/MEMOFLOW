pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repositório oficial para o SQLCipher (usando HTTPS)
        maven { url = uri("https://www.zetetic.net/maven/release/") }
    }
}

rootProject.name = "Memo Flow"
include(":app")
