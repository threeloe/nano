#publish libraries
./gradlew :nano:publishMavenNanoPublicationToMavenRepository
./gradlew :nano_impl:publishMavenNanoPublicationToMavenRepository
./gradlew :nano_api:publishMavenNanoPublicationToMavenRepository

#publish gradle plugin
cd buildSrc
../gradlew publishMavenNanoPublicationToMavenRepository