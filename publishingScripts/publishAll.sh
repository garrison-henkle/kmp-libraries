#!/usr/bin/env bash
./gradlew clean
# Root Group
./gradlew :context-provider:publishAllPublicationsToMavenHenkleDevReleasesRepository
./gradlew :keymp:publishAllPublicationsToMavenHenkleDevReleasesRepository
./gradlew :keymp-preferences:publishAllPublicationsToMavenHenkleDevReleasesRepository
./gradlew :stytch-kmp:publishAllPublicationsToMavenHenkleDevReleasesRepository
./gradlew :korvus:publishAllPublicationsToMavenHenkleDevReleasesRepository

# Compose Group
./gradlew :better-bottom-sheet:publishAllPublicationsToMavenHenkleDevReleasesRepository
./gradlew :pager:publishAllPublicationsToMavenHenkleDevReleasesRepository
