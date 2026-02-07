#!/bin/sh
set -e
echo "building application"
cd my-lambda && ../gradlew clean build
echo "building CDK"
cd ../cdk && ../gradlew clean build && cdk deploy --all --require-approval=never
