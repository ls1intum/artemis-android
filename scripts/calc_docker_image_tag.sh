#!/bin/bash

regex='distributionUrl=https\\\:\/\/services.gradle.org\/distributions\/gradle-(.*)-bin.zip'
gradleDistributionLine=$(grep -oP $regex gradle/wrapper/gradle-wrapper.properties)

[[ $gradleDistributionLine =~ $regex ]]
gradleVersion=${BASH_REMATCH[1]}

regex='(.*)\s.*'
versionCatalogHashOutput=$(sha1sum gradle/libs.versions.toml)
[[ $versionCatalogHashOutput =~ $regex ]]

versionCatalogHash=${BASH_REMATCH[1]}
echo "${gradleVersion}-${versionCatalogHash}"
