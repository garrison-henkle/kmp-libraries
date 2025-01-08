#!/usr/bin/env bash
./gradlew clean
# To maven.henkle.dev
./gradlew :kmarkdownp-ui:publishKotlinMultiplatformPublicationToMavenHenkleDevReleasesRepository
./gradlew :kmarkdownp-ui:publishIosArm64PublicationToMavenHenkleDevReleasesRepository
./gradlew :kmarkdownp-ui:publishIosSimulatorArm64PublicationToMavenHenkleDevReleasesRepository
./gradlew :kmarkdownp-ui:publishAndroidReleasePublicationToMavenHenkleDevReleasesRepository
# To GitHub Packages
./gradlew :kmarkdownp-ui:publishKotlinMultiplatformPublicationToKMPBrowserGitHubPackagesRepository
./gradlew :kmarkdownp-ui:publishIosArm64PublicationToKMPBrowserGitHubPackagesRepository
./gradlew :kmarkdownp-ui:publishIosSimulatorArm64PublicationToKMPBrowserGitHubPackagesRepository
./gradlew :kmarkdownp-ui:publishAndroidReleasePublicationToKMPBrowserGitHubPackagesRepository