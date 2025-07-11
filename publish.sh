#publish libraries
./gradlew :nano:publishReleasePublicationToMavenRepository
./gradlew :nano_core:publishReleasePublicationToMavenRepository
./gradlew :nano_api:publishReleasePublicationToMavenRepository

#publish gradle plugin
./gradlew :nano_plugin:publishReleasePublicationToMavenRepository