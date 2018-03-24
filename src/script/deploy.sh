#! /bin/sh
#
# This script deploys the artifacts for j4cups to https://oss.sonatype.org/
# see https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7b.StageExistingArtifacts
#
# (c)reated: 09-Feb-2018 by boehm@javatux.de
#

# set up some constants
URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
VERSION=0.3.0
options="gpg:sign-and-deploy-file -Durl=$URL -DrepositoryId=sonatype-nexus-staging"

# passphrase is needed for signing
echo "passphrase for GPG: "
stty_orig=`stty -g`
stty -echo
read passphrase
stty $stty_orig

options="gpg:sign-and-deploy-file -Durl=$URL -DrepositoryId=sonatype-nexus-staging -Dgpg.passphrase=$passphrase"

deploy_jar_for() {
	module=$1
	echo deploying $module...
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION.jar
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION-sources.jar -Dclassifier=sources
    mvn $options -DpomFile=target/$module-$VERSION.pom -Dfile=target/$module-$VERSION-javadoc.jar -Dclassifier=javadoc
    echo
}

# start deployment
deploy_jar_for j4cups
