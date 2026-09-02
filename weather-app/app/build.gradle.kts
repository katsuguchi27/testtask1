// Determine for Gradle kotlin version and repositories for JVM
plugins { 
    kotlin("jvm") version "2.3.0"
    application
}
repositories {
    mavenCentral()
}
//Determine the dependencies for work
dependencies {
    //Retrofit and Jackson converters
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
application {
    mainClass.set("org.example.MainKt")
}
