#!/usr/bin/env bash
./gradlew clean
./gradlew :nanoid:publishAllPublicationsToMavenHenkleDevReleasesRepository
